package com.example.dnd35.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd35.backup.BackupFileException
import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.backup.CharacterBackupIo
import com.example.dnd35.data.AppDatabase
import com.example.dnd35.data.CharacterRepository
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.LanguageAcquisition
import com.example.dnd35.model.CharacterFeat
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSkill
import com.example.dnd35.model.SkillPointLevelRecord
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.SpellSlotInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal fun isValidEditedSkillRanks(ranks: Float): Boolean {
    if (!ranks.isFinite() || ranks < 0f) return false
    val doubled = ranks * 2f
    return kotlin.math.abs(doubled - doubled.toInt()) < 0.0001f
}

internal fun normalizeEditedSkillRanks(ranks: Float, maxRanks: Float): Float {
    val safeMax = if (maxRanks.isFinite()) maxRanks.coerceAtLeast(0f) else 0f
    val safeRanks = if (ranks.isFinite()) ranks.coerceAtLeast(0f) else 0f
    // Defesa para chamadas internas: nunca concede graduação arredondando para cima.
    return ((safeRanks * 2f).toInt().coerceAtLeast(0) / 2f).coerceAtMost(safeMax)
}

/** Passo de edição preserva meios pontos antigos mesmo quando a perícia passa a ser de classe. */
internal fun skillRankAdjustmentStep(ranks: Float, isClassSkill: Boolean): Float =
    if (!isClassSkill || ranks % 1f != 0f) 0.5f else 1.0f

internal data class ConvertedCoinBalances(
    val goldGp: Float,
    val silverSp: Float,
    val copperCp: Float,
    val platinumPp: Float = 0f
)

/**
 * Consolidates denominations without discarding fractional copper value.
 * Returns null for invalid balances rather than silently changing them.
 */
internal fun convertCoinBalancesToGold(
    goldGp: Float,
    silverSp: Float,
    copperCp: Float,
    platinumPp: Float
): ConvertedCoinBalances? {
    val balances = listOf(goldGp, silverSp, copperCp, platinumPp)
    if (balances.any { !it.isFinite() || it < 0f }) return null

    // Work in copper-equivalent units using Double to reduce rounding artifacts.
    val totalCopper = goldGp.toDouble() * 100.0 +
        silverSp.toDouble() * 10.0 +
        copperCp.toDouble() +
        platinumPp.toDouble() * 1000.0
    if (!totalCopper.isFinite()) return null

    val gold = kotlin.math.floor(totalCopper / 100.0)
    val afterGold = totalCopper - gold * 100.0
    val silver = kotlin.math.floor(afterGold / 10.0)
    val copper = afterGold - silver * 10.0

    return ConvertedCoinBalances(
        goldGp = gold.toFloat(),
        silverSp = silver.toFloat(),
        copperCp = copper.toFloat(),
        platinumPp = 0f
    )
}

internal data class HpAdjustment(val currentHp: Int, val tempHp: Int)

/**
 * Applies healing or damage to a character. Damage is absorbed by temporary HP
 * first; healing restores only regular HP and never raises it above max HP.
 */
internal fun calculateHpAdjustment(currentHp: Int, maxHp: Int, tempHp: Int, delta: Int): HpAdjustment {
    val safeMaxHp = maxHp.coerceAtLeast(0)
    val safeCurrentHp = currentHp.coerceIn(0, safeMaxHp)
    val safeTempHp = tempHp.coerceAtLeast(0)

    if (delta >= 0) {
        val healed = (safeCurrentHp.toLong() + delta.toLong()).coerceAtMost(safeMaxHp.toLong()).toInt()
        return HpAdjustment(currentHp = healed, tempHp = safeTempHp)
    }

    val damage = -delta.toLong()
    val absorbedByTemp = minOf(safeTempHp.toLong(), damage).toInt()
    val remainingDamage = damage - absorbedByTemp
    val newHp = (safeCurrentHp.toLong() - remainingDamage).coerceAtLeast(0L).toInt()
    return HpAdjustment(currentHp = newHp, tempHp = safeTempHp - absorbedByTemp)
}

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    // Serializa snapshots do personagem para que gravações concorrentes não sobrescrevam estados mais recentes.
    private val characterSaveMutex = Mutex()
    private val repository: CharacterRepository

    val allCharacters: StateFlow<List<CharacterEntity>>
    private val _selectedCharacterId = MutableStateFlow<Long?>(null)
    val selectedCharacterId = _selectedCharacterId.asStateFlow()

    private val _activeCharacter = MutableStateFlow<CharacterEntity?>(null)
    val activeCharacter = _activeCharacter.asStateFlow()

    private val _computedStats = MutableStateFlow<CalculatedCharacterStats?>(null)
    val computedStats = _computedStats.asStateFlow()

    private val _backupMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    /** Mensagens curtas para o usuário sobre exportação e importação de fichas. */
    val backupMessages: SharedFlow<String> = _backupMessages.asSharedFlow()

    init {
        val dao = AppDatabase.getDatabase(application).characterDao()
        repository = CharacterRepository(dao)

        allCharacters = repository.allCharacters.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            allCharacters.collect { list ->
                if (list.isNotEmpty()) {
                    val currentId = _selectedCharacterId.value
                    if (currentId == null || list.none { it.id == currentId }) {
                        val first = list.first()
                        _selectedCharacterId.value = first.id
                        _activeCharacter.value = first
                        _computedStats.value = CharacterCalculator.compute(first)
                    } else {
                        val updated = list.first { it.id == currentId }
                        _activeCharacter.value = updated
                        _computedStats.value = CharacterCalculator.compute(updated)
                    }
                } else {
                    _activeCharacter.value = null
                    _computedStats.value = null
                }
            }
        }
    }

    fun selectCharacter(id: Long) {
        _selectedCharacterId.value = id
        val found = allCharacters.value.find { it.id == id }
        if (found != null) {
            _activeCharacter.value = found
            _computedStats.value = CharacterCalculator.compute(found)
        }
    }

    private fun persistCharacter(char: CharacterEntity) {
        _activeCharacter.value = char
        _computedStats.value = CharacterCalculator.compute(char)
        viewModelScope.launch {
            characterSaveMutex.withLock {
                withContext(Dispatchers.IO) { repository.saveCharacter(char) }
            }
        }
    }

    // Gerenciamento geral de personagem
    fun updateGeneralInfo(
        name: String,
        characterClass: String,
        race: String,
        level: Int,
        alignment: String,
        deity: String,
        size: String
    ) {
        val curr = _activeCharacter.value ?: return
        val currentClasses = CharacterJsonAdapters.deserializeClassEntries(curr.multiclassJson, curr.characterClass, curr.level)
        val updatedClasses = if (currentClasses.size <= 1) {
            listOf(com.example.dnd35.model.CharacterClassEntry(characterClass, level.coerceAtLeast(1)))
        } else {
            currentClasses
        }
        val totalLvl = updatedClasses.sumOf { it.level }.coerceAtLeast(1)
        val classDisplay = updatedClasses.joinToString(" / ") { "${it.className} ${it.level}" }
        val updated = curr.copy(
            name = name,
            characterClass = classDisplay,
            race = race,
            level = totalLvl,
            multiclassJson = CharacterJsonAdapters.serializeClassEntries(updatedClasses),
            alignment = alignment,
            deity = deity,
            size = size
        )
        persistCharacter(updated)
    }

    fun updateClasses(classes: List<com.example.dnd35.model.CharacterClassEntry>) {
        val curr = _activeCharacter.value ?: return
        if (classes.isEmpty()) return
        val totalLvl = classes.sumOf { it.level }.coerceAtLeast(1)
        val displayClass = classes.joinToString(" / ") { "${it.className} ${it.level}" }
        val json = CharacterJsonAdapters.serializeClassEntries(classes)
        val updated = curr.copy(
            multiclassJson = json,
            characterClass = displayClass,
            level = totalLvl
        )
        persistCharacter(updated)
    }

    fun addClass(className: String, initialLevel: Int = 1) {
        val curr = _activeCharacter.value ?: return
        val currentClasses = CharacterJsonAdapters.deserializeClassEntries(curr.multiclassJson, curr.characterClass, curr.level).toMutableList()
        val existingIndex = currentClasses.indexOfFirst { it.className.equals(className, ignoreCase = true) }
        if (existingIndex >= 0) {
            val existing = currentClasses[existingIndex]
            currentClasses[existingIndex] = existing.copy(level = existing.level + initialLevel)
        } else {
            currentClasses.add(com.example.dnd35.model.CharacterClassEntry(className, initialLevel))
        }
        updateClasses(currentClasses)
    }

    fun setClassLevel(className: String, newLevel: Int) {
        val curr = _activeCharacter.value ?: return
        val currentClasses = CharacterJsonAdapters.deserializeClassEntries(curr.multiclassJson, curr.characterClass, curr.level).toMutableList()
        val existingIndex = currentClasses.indexOfFirst { it.className.equals(className, ignoreCase = true) }
        if (existingIndex >= 0) {
            if (newLevel <= 0) {
                if (currentClasses.size > 1) {
                    currentClasses.removeAt(existingIndex)
                } else {
                    currentClasses[existingIndex] = currentClasses[existingIndex].copy(level = 1)
                }
            } else {
                currentClasses[existingIndex] = currentClasses[existingIndex].copy(level = newLevel)
            }
            updateClasses(currentClasses)
        }
    }

    fun removeClass(className: String) {
        val curr = _activeCharacter.value ?: return
        val currentClasses = CharacterJsonAdapters.deserializeClassEntries(curr.multiclassJson, curr.characterClass, curr.level).toMutableList()
        if (currentClasses.size > 1) {
            currentClasses.removeAll { it.className.equals(className, ignoreCase = true) }
            updateClasses(currentClasses)
        }
    }

    fun createCharacter(
        name: String,
        characterClass: String,
        race: String,
        baseStr: Int = 14,
        baseDex: Int = 12,
        baseCon: Int = 14,
        baseInt: Int = 10,
        baseWis: Int = 10,
        baseCha: Int = 10
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.createNewCharacter(
                name, characterClass, race,
                baseStr, baseDex, baseCon, baseInt, baseWis, baseCha
            )
            _selectedCharacterId.value = newId
        }
    }

    fun duplicateCurrentCharacter() {
        val curr = _activeCharacter.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.duplicateCharacter(curr)
            _selectedCharacterId.value = newId
        }
    }

    fun deleteCurrentCharacter() {
        val curr = _activeCharacter.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCharacter(curr.id)
            _selectedCharacterId.value = null
        }
    }

    // Gestão de Pontos de Vida
    fun adjustHp(delta: Int) {
        val curr = _activeCharacter.value ?: return
        val adjusted = calculateHpAdjustment(
            currentHp = curr.currentHp,
            maxHp = curr.maxHp,
            tempHp = curr.tempHp,
            delta = delta
        )
        persistCharacter(curr.copy(currentHp = adjusted.currentHp, tempHp = adjusted.tempHp))
    }

    fun setMaxHp(maxHp: Int) {
        val curr = _activeCharacter.value ?: return
        val newMax = maxHp.coerceAtLeast(1)
        val newCurrent = curr.currentHp.coerceAtMost(newMax)
        persistCharacter(curr.copy(maxHp = newMax, currentHp = newCurrent))
    }

    fun setTempHp(temp: Int) {
        val curr = _activeCharacter.value ?: return
        persistCharacter(curr.copy(tempHp = temp.coerceAtLeast(0)))
    }

    fun setNonLethal(nonLethal: Int) {
        val curr = _activeCharacter.value ?: return
        persistCharacter(curr.copy(nonLethalDamage = nonLethal.coerceAtLeast(0)))
    }

    fun longRest() {
        val curr = _activeCharacter.value ?: return
        val slots = CharacterJsonAdapters.deserializeSpellSlots(curr.spellSlotsJson).map {
            it.copy(usedSlots = 0)
        }
        val restored = curr.copy(
            currentHp = curr.maxHp,
            tempHp = 0,
            nonLethalDamage = 0,
            spellSlotsJson = CharacterJsonAdapters.serializeSpellSlots(slots)
        )
        persistCharacter(restored)
    }

    // Atributos
    fun updateAbility(ability: String, baseVal: Int, magicVal: Int) {
        val curr = _activeCharacter.value ?: return
        val updated = when (ability.uppercase()) {
            "FOR" -> curr.copy(baseStr = baseVal, modStrBonus = magicVal)
            "DES" -> curr.copy(baseDex = baseVal, modDexBonus = magicVal)
            "CON" -> curr.copy(baseCon = baseVal, modConBonus = magicVal)
            "INT" -> curr.copy(baseInt = baseVal, modIntBonus = magicVal)
            "SAB" -> curr.copy(baseWis = baseVal, modWisBonus = magicVal)
            "CAR" -> curr.copy(baseCha = baseVal, modChaBonus = magicVal)
            else -> curr
        }
        persistCharacter(updated)
    }

    // Defesas e CA
    fun updateArmorValues(
        armorBonus: Int,
        shieldBonus: Int,
        naturalArmor: Int,
        deflectionBonus: Int,
        miscAcBonus: Int,
        miscFort: Int,
        miscRef: Int,
        miscWill: Int,
        miscInit: Int
    ) {
        val curr = _activeCharacter.value ?: return
        val updated = curr.copy(
            armorBonus = armorBonus,
            shieldBonus = shieldBonus,
            naturalArmor = naturalArmor,
            deflectionBonus = deflectionBonus,
            miscAcBonus = miscAcBonus,
            miscFortitude = miscFort,
            miscReflex = miscRef,
            miscWill = miscWill,
            miscInitiative = miscInit
        )
        persistCharacter(updated)
    }

    // Perícias
    fun updateSkill(skillName: String, ranks: Float, miscBonus: Int, isClassSkill: Boolean) {
        val curr = _activeCharacter.value ?: return
        val currentSkills = CharacterJsonAdapters.deserializeSkills(curr.skillsJson).toMutableList()
        val definition = com.example.dnd35.model.Dnd35Rules.SKILL_DEFINITIONS
            .find { it.name.equals(skillName, ignoreCase = true) }
        val classEntries = CharacterJsonAdapters.deserializeClassEntries(
            curr.multiclassJson, curr.characterClass, curr.level
        )
        val actualClassSkill = if (definition != null || com.example.dnd35.model.Dnd35Rules.isCraftSpecialty(skillName)) {
            classEntries.any { entry ->
                com.example.dnd35.model.Dnd35Rules.CLASSES
                    .find { it.name.equals(entry.className, ignoreCase = true) }
                    ?.classSkills?.any {
                        com.example.dnd35.model.Dnd35Rules.classSkillMatches(skillName, it)
                    } == true
            }
        } else isClassSkill
        val maxClassRanks = classEntries.sumOf { it.level }.coerceAtLeast(1) + 3f
        val maxRanks = if (actualClassSkill) maxClassRanks else maxClassRanks / 2f
        // Graduações de 0,5 podem existir antes de a perícia passar a ser de classe
        // por multiclasse; a edição não deve arredondá-las para cima.
        val normalizedRanks = normalizeEditedSkillRanks(ranks, maxRanks)
        val index = currentSkills.indexOfFirst { it.name.equals(skillName, ignoreCase = true) }
        if (index >= 0) {
            val old = currentSkills[index]
            currentSkills[index] = old.copy(
                keyAbility = com.example.dnd35.model.Dnd35Rules.canonicalSkillKeyAbility(skillName, old.keyAbility),
                ranks = normalizedRanks,
                miscBonus = miscBonus,
                isClassSkill = actualClassSkill
            )
        } else {
            val key = com.example.dnd35.model.Dnd35Rules.canonicalSkillKeyAbility(skillName, "FOR")
            currentSkills.add(CharacterSkill(
                name = skillName, keyAbility = key, ranks = normalizedRanks,
                miscBonus = miscBonus, isClassSkill = actualClassSkill
            ))
        }
        persistCharacter(curr.copy(skillsJson = CharacterJsonAdapters.serializeSkills(currentSkills)))
    }

    // Inventário
    fun addInventoryItem(item: CharacterInventoryItem) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson).toMutableList()
        list.add(item)
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(list)))
    }

    /** Atualiza um item em uma única gravação, sem a corrida remove/adiciona. */
    fun updateInventoryItem(item: CharacterInventoryItem) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson)
        if (list.none { it.id == item.id }) return
        val updated = list.map { existing -> if (existing.id == item.id) item else existing }
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(updated)))
    }

    fun removeInventoryItem(itemId: String) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson).filter { it.id != itemId }
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(list)))
    }

    /** Coloca uma pilha escolhida pelo jogador no slot global de munição de combate. */
    fun equipAmmunition(itemId: String) {
        val curr = _activeCharacter.value ?: return
        val items = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson)
        val selected = items.firstOrNull { it.id == itemId && it.quantity > 0 && it.inBackpack } ?: return
        val ammoSlot = com.example.dnd35.model.Dnd35Rules.EquipmentSlots.AMMUNITION
        val updated = items.map { item ->
            when {
                item.id == selected.id -> item.copy(equipped = false, equippedSlot = ammoSlot)
                item.equippedSlot == ammoSlot -> item.copy(equipped = false, equippedSlot = com.example.dnd35.model.Dnd35Rules.EquipmentSlots.NONE)
                else -> item
            }
        }
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(updated)))
    }

    /** Consome uma unidade somente da pilha escolhida no slot de munição. */
    fun consumeEquippedAmmunition(): Boolean {
        val curr = _activeCharacter.value ?: return false
        val items = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson)
        val ammunition = items.firstOrNull {
            it.equippedSlot == com.example.dnd35.model.Dnd35Rules.EquipmentSlots.AMMUNITION &&
                it.quantity > 0 && com.example.dnd35.model.remainingAmmunitionUnits(it) > 0
        } ?: return false
        val updated = com.example.dnd35.model.consumeAmmunitionStack(items, ammunition.id) ?: return false
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(updated)))
        return true
    }

    /** Pega ou solta os itens guardados na mochila sem alterar itens equipados no corpo/combate. */
    fun setBackpackContentsCarried(carried: Boolean) {
        val curr = _activeCharacter.value ?: return
        val items = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson)
        val updated = items.map { item ->
            if (!item.equipped && item.equippedSlot == com.example.dnd35.model.Dnd35Rules.EquipmentSlots.NONE) {
                item.copy(inBackpack = carried)
            } else item
        }
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(updated)))
    }

    fun toggleEquipItem(itemId: String) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson).map {
            if (it.id == itemId) {
                val willEquip = !it.equipped
                val slot = if (willEquip) {
                    if (it.equippedSlot != com.example.dnd35.model.Dnd35Rules.EquipmentSlots.NONE) it.equippedSlot
                    else when (it.category.lowercase()) {
                        "armas", "arma" -> com.example.dnd35.model.Dnd35Rules.EquipmentSlots.MAIN_HAND
                        "armaduras", "armadura" -> com.example.dnd35.model.Dnd35Rules.EquipmentSlots.ARMOR_BODY
                        "escudos", "escudo" -> com.example.dnd35.model.Dnd35Rules.EquipmentSlots.OFF_HAND
                        else -> com.example.dnd35.model.Dnd35Rules.EquipmentSlots.MAIN_HAND
                    }
                } else {
                    com.example.dnd35.model.Dnd35Rules.EquipmentSlots.NONE
                }
                it.copy(equipped = willEquip, equippedSlot = slot)
            } else it
        }
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(list)))
    }

    fun assignItemSlot(itemId: String, targetSlot: String) {
        val curr = _activeCharacter.value ?: return
        val isUnequipping = targetSlot == com.example.dnd35.model.Dnd35Rules.EquipmentSlots.NONE
        val list = CharacterJsonAdapters.deserializeInventory(curr.inventoryJson).map {
            if (it.id == itemId) {
                it.copy(
                    equipped = !isUnequipping,
                    equippedSlot = targetSlot
                )
            } else if (!isUnequipping && it.equippedSlot == targetSlot) {
                // Desequipar o item que já ocupava esse slot exclusivo
                it.copy(
                    equipped = false,
                    equippedSlot = com.example.dnd35.model.Dnd35Rules.EquipmentSlots.NONE
                )
            } else it
        }
        persistCharacter(curr.copy(inventoryJson = CharacterJsonAdapters.serializeInventory(list)))
    }

    fun updateCoins(gp: Float, sp: Float, cp: Float, pp: Float) {
        val curr = _activeCharacter.value ?: return
        persistCharacter(curr.copy(goldGp = gp, silverSp = sp, copperCp = cp, platinumPp = pp))
    }

    fun quickCoinAction(deltaGp: Float) {
        val curr = _activeCharacter.value ?: return
        val newGp = (curr.goldGp + deltaGp).coerceAtLeast(0f)
        persistCharacter(curr.copy(goldGp = newGp))
    }

    fun convertCoinsToGold() {
        val curr = _activeCharacter.value ?: return
        val converted = convertCoinBalancesToGold(
            goldGp = curr.goldGp,
            silverSp = curr.silverSp,
            copperCp = curr.copperCp,
            platinumPp = curr.platinumPp
        ) ?: return
        persistCharacter(
            curr.copy(
                goldGp = converted.goldGp,
                silverSp = converted.silverSp,
                copperCp = converted.copperCp,
                platinumPp = converted.platinumPp
            )
        )
    }

    // Talentos
    fun addFeat(feat: CharacterFeat) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeFeats(curr.featsJson).toMutableList()
        list.add(feat)
        persistCharacter(curr.copy(featsJson = CharacterJsonAdapters.serializeFeats(list)))
    }

    fun removeFeat(featId: String) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeFeats(curr.featsJson).filter { it.id != featId }
        persistCharacter(curr.copy(featsJson = CharacterJsonAdapters.serializeFeats(list)))
    }

    // Magias
    fun addSpell(spell: CharacterSpell) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeSpells(curr.spellsJson).toMutableList()
        list.add(spell)
        persistCharacter(curr.copy(spellsJson = CharacterJsonAdapters.serializeSpells(list)))
    }

    fun removeSpell(spellId: String) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeSpells(curr.spellsJson).filter { it.id != spellId }
        persistCharacter(curr.copy(spellsJson = CharacterJsonAdapters.serializeSpells(list)))
    }

    fun toggleSpellPrepared(spellId: String) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeSpells(curr.spellsJson).map {
            if (it.id == spellId) it.copy(prepared = !it.prepared) else it
        }
        persistCharacter(curr.copy(spellsJson = CharacterJsonAdapters.serializeSpells(list)))
    }

    fun adjustSpellSlotUsed(level: Int, delta: Int) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeSpellSlots(curr.spellSlotsJson).map {
            if (it.level == level) {
                val newUsed = (it.usedSlots + delta).coerceIn(0, it.totalSlots)
                it.copy(usedSlots = newUsed)
            } else it
        }
        persistCharacter(curr.copy(spellSlotsJson = CharacterJsonAdapters.serializeSpellSlots(list)))
    }

    fun setSpellSlotTotal(level: Int, total: Int) {
        val curr = _activeCharacter.value ?: return
        val list = CharacterJsonAdapters.deserializeSpellSlots(curr.spellSlotsJson).map {
            if (it.level == level) {
                val newTotal = total.coerceAtLeast(0)
                it.copy(totalSlots = newTotal, usedSlots = it.usedSlots.coerceAtMost(newTotal))
            } else it
        }
        persistCharacter(curr.copy(spellSlotsJson = CharacterJsonAdapters.serializeSpellSlots(list)))
    }

    // Foto do personagem
    fun setPhotoUri(uri: String) {
        val curr = _activeCharacter.value ?: return
        persistCharacter(curr.copy(photoUri = uri))
    }

    fun updateLanguages(languages: List<String>) {
        val curr = _activeCharacter.value ?: return
        persistCharacter(curr.copy(languagesJson = CharacterJsonAdapters.serializeLanguages(languages)))
    }

    fun updateLanguageAcquisitions(records: List<LanguageAcquisition>) {
        val curr = _activeCharacter.value ?: return
        val normalized = records
            .filter { it.isValid() }
            .distinctBy { it.language.trim().lowercase() }
            .map { it.copy(language = it.language.trim()) }
        persistCharacter(
            curr.copy(languageAcquisitionsJson = CharacterJsonAdapters.serializeLanguageAcquisitions(normalized))
        )
    }

    fun updateSkillPointHistory(records: List<SkillPointLevelRecord>) {
        val curr = _activeCharacter.value ?: return
        val normalized = records
            .filter { it.characterLevel >= 1 && it.className.isNotBlank() && it.availablePoints.isFinite() && it.availablePoints >= 0f }
            .distinctBy { it.characterLevel }
            .map { record ->
                record.copy(
                    className = record.className.trim(),
                    allocations = record.allocations.filter {
                        it.skillName.isNotBlank() && it.pointsSpent.isFinite() && it.pointsSpent >= 0f
                    }.map { it.copy(skillName = it.skillName.trim()) }
                )
            }
        persistCharacter(
            curr.copy(skillPointHistoryJson = CharacterJsonAdapters.serializeSkillPointHistory(normalized))
        )
    }

    /** Exporta a ficha ativa ou todas as fichas para o arquivo escolhido pelo usuário. */
    fun exportBackup(target: Uri, allCharacters: Boolean) {
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = if (allCharacters) {
                    repository.getAllCharactersOnce()
                } else {
                    listOfNotNull(_activeCharacter.value)
                }
                if (list.isEmpty()) {
                    _backupMessages.emit("Não há fichas para exportar.")
                    return@launch
                }
                CharacterBackupIo.writeBackup(app, target, list)
                _backupMessages.emit(
                    if (list.size == 1) "Ficha exportada." else "${list.size} fichas exportadas."
                )
            } catch (e: Exception) {
                _backupMessages.emit("Erro ao exportar: ${e.localizedMessage ?: "falha desconhecida"}")
            }
        }
    }

    /** Importa fichas de um arquivo de backup. Nunca sobrescreve fichas existentes. */
    fun importBackup(source: Uri) {
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = CharacterBackupIo.readBackupText(app, source)
                when (val parsed = CharacterBackup.parse(text)) {
                    is CharacterBackup.ParseResult.Failure -> _backupMessages.emit(parsed.message)
                    is CharacterBackup.ParseResult.Success -> {
                        val warnings = parsed.warnings.toMutableList()
                        val entities = parsed.characters.mapIndexed { index, imported ->
                            val bytes = imported.photoBytes
                            val photo = bytes?.let { CharacterBackupIo.savePortrait(app, it, index) }
                            if (bytes != null && photo == null) {
                                warnings += "${imported.entity.name}: retrato não pôde ser importado."
                            }
                            if (photo != null) imported.entity.copy(photoUri = photo) else imported.entity
                        }
                        val ids = repository.importCharacters(entities)
                        ids.lastOrNull()?.let { _selectedCharacterId.value = it }
                        val base = if (entities.size == 1) "1 ficha importada." else "${entities.size} fichas importadas."
                        val extra = if (warnings.isEmpty()) "" else
                            " Avisos: " + warnings.take(2).joinToString(" ") +
                                if (warnings.size > 2) " (+${warnings.size - 2})" else ""
                        _backupMessages.emit(base + extra)
                    }
                }
            } catch (e: BackupFileException) {
                _backupMessages.emit(e.message ?: "Arquivo de backup inválido.")
            } catch (e: Exception) {
                _backupMessages.emit("Erro ao importar: ${e.localizedMessage ?: "falha desconhecida"}")
            }
        }
    }

    fun updateNotes(notes: String) {
        val curr = _activeCharacter.value ?: return
        persistCharacter(curr.copy(notes = notes))
    }
}
