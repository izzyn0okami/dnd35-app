package com.example.dnd35.model

import kotlin.math.roundToInt

data class ClassDetailInfo(
    val entry: CharacterClassEntry,
    val classInfo: Dnd35Rules.ClassInfo,
    val babContribution: Int,
    val baseFort: Int,
    val baseRef: Int,
    val baseWill: Int,
    val hitDie: Int,
    val isCaster: Boolean,
    val isFavored: Boolean
)

data class MulticlassPenaltyInfo(
    val hasPenalty: Boolean,
    val penaltyPercent: Int,
    val favoredClass: String,
    val explanation: String
)

data class CasterClassInfo(
    val className: String,
    val casterLevel: Int,
    val keyAbility: String,
    val keyAbilityMod: Int,
    val dcByLevel: Map<Int, Int>
)

data class CalculatedCharacterStats(
    val character: CharacterEntity,
    val raceInfo: Dnd35Rules.RaceInfo,
    val classInfo: Dnd35Rules.ClassInfo,
    val classEntries: List<CharacterClassEntry>,
    val multiclassDetails: List<ClassDetailInfo>,
    val multiclassPenalty: MulticlassPenaltyInfo,
    val isMulticlass: Boolean,
    val classesDisplay: String,
    val totalCharacterLevel: Int,
    val skills: List<CharacterSkill>,
    val inventory: List<CharacterInventoryItem>,
    val feats: List<CharacterFeat>,
    val spells: List<CharacterSpell>,
    val spellSlots: List<SpellSlotInfo>,

    // Atributos Finais (Base + Raça + Equipamentos/Mágicos)
    val totalStr: Int,
    val strMod: Int,
    val totalDex: Int,
    val dexMod: Int,
    val totalCon: Int,
    val conMod: Int,
    val totalInt: Int,
    val intMod: Int,
    val totalWis: Int,
    val wisMod: Int,
    val totalCha: Int,
    val chaMod: Int,

    // BBA & Ataques
    val bab: Int,
    val babDisplay: String,
    val grappleBonus: Int,

    // Resistências
    val baseFort: Int,
    val totalFort: Int,
    val baseRef: Int,
    val totalRef: Int,
    val baseWill: Int,
    val totalWill: Int,

    // Iniciativa & Velocidade
    val initiativeTotal: Int,
    val speedMetersEffective: Float,

    // CA
    val armorBonusTotal: Int,
    val shieldBonusTotal: Int,
    val naturalArmorTotal: Int,
    val deflectionBonusTotal: Int,
    val maxDexFromArmor: Int?,
    val effectiveDexForAc: Int,
    val armorCheckPenalty: Int,
    val totalAc: Int,
    val touchAc: Int,
    val flatFootedAc: Int,

    // Habilidades Especiais Ativas da Classe
    val activeClassFeatures: List<Dnd35Rules.ClassFeature>,
    val sneakAttackDice: String?, // Ex: "+2d6" para Ladino
    val monkUnarmedDamage: String?, // Ex: "1d8" para Monge
    val rageUsesPerDay: Int?, // Para Bárbaro
    val turningUsesPerDay: Int?, // Para Clérigo/Paladino: 3 + mod Car

    // Conjurador & CD de Magias
    val isSpellcaster: Boolean,
    val castingAbilityName: String?,
    val castingAbilityMod: Int,
    val spellSaveDcByLevel: Map<Int, Int>, // Círculo -> 10 + círculo + mod atributo
    val casterClasses: List<CasterClassInfo>,

    // Carga & Inventário
    val totalWeightLb: Float,
    val carryingCapacity: Dnd35Rules.CarryingCapacity,
    val encumbrance: Dnd35Rules.EncumbranceLevel,

    // Perícias Calculadas
    val computedSkills: List<ComputedSkill>
)

data class ComputedSkill(
    val skill: CharacterSkill,
    val keyAbilityMod: Int,
    val ranks: Float,
    val racialBonus: Int,
    val sizeBonus: Int,
    val miscBonus: Int,
    val armorPenaltyApplied: Int,
    val totalBonus: Int,
    val isClassSkill: Boolean,
    val maxRanks: Float,
    val trainedOnly: Boolean,
    val canRoll: Boolean
)

object CharacterCalculator {

    private fun monkUnarmedDamageForSize(level: Int, sizeName: String): String {
        val tier = when {
            level >= 20 -> 5
            level >= 16 -> 4
            level >= 12 -> 3
            level >= 8 -> 2
            level >= 4 -> 1
            else -> 0
        }
        val size = sizeName.trim().lowercase()
        val damageBySize = when (size) {
            "pequeno" -> listOf("1d4", "1d6", "1d8", "1d10", "2d6", "2d8")
            "grande" -> listOf("1d8", "2d6", "2d8", "3d6", "3d8", "4d8")
            else -> listOf("1d6", "1d8", "1d10", "2d6", "2d8", "2d10")
        }
        return damageBySize[tier]
    }

    private fun sizeSkillBonus(sizeName: String, skillName: String): Int {
        if (!skillName.equals("Esconder-se", ignoreCase = true)) return 0
        return when (sizeName.trim().lowercase()) {
            "minúsculo" -> 16
            "diminuto" -> 12
            "miúdo" -> 8
            "pequeno" -> 4
            "médio" -> 0
            "grande" -> -4
            "enorme" -> -8
            "imenso" -> -12
            "colossal" -> -16
            else -> 0
        }
    }

    private fun racialSkillBonus(raceName: String, skillName: String): Int {
        val race = raceName.trim()
        val skill = Dnd35Rules.canonicalSkillName(skillName).trim()

        return when {
            race.equals("Meio-Elfo", ignoreCase = true) && skill in setOf(
                "Ouvir", "Procurar", "Observar"
            ) -> 1
            race.equals("Meio-Elfo", ignoreCase = true) && skill in setOf(
                "Diplomacia", "Obter Informação"
            ) -> 2
            (race.contains("Elfo", ignoreCase = true) || race.contains("Drow", ignoreCase = true)) &&
                skill in setOf("Ouvir", "Procurar", "Observar") -> 2
            race.equals("Halfling Robusto", ignoreCase = true) &&
                skill in setOf("Escalar", "Saltar", "Ouvir", "Furtividade") -> 2
            race.equals("Halfling", ignoreCase = true) &&
                skill in setOf("Escalar", "Saltar", "Ouvir", "Furtividade") -> 2
            race.equals("Gnomo", ignoreCase = true) && (
                skill.equals("Ouvir", ignoreCase = true) ||
                    skill.equals("Ofícios (Alquimia)", ignoreCase = true)
            ) -> 2
            else -> 0
        }
    }

    fun compute(character: CharacterEntity): CalculatedCharacterStats {
        val race = Dnd35Rules.RACES.find { it.name.equals(character.race, ignoreCase = true) }
            ?: Dnd35Rules.RACES.first()

        // 1. Desserializar classes da multiclasse
        val classEntries = CharacterJsonAdapters.deserializeClassEntries(
            character.multiclassJson,
            fallbackClass = character.characterClass.split("/").firstOrNull()?.trim()?.split(" ")?.firstOrNull()?.trim() ?: character.characterClass,
            fallbackLevel = character.level
        )

        val totalCharacterLevel = classEntries.sumOf { it.level }.coerceAtLeast(1)
        val isMulticlass = classEntries.size > 1
        val classesDisplay = classEntries.joinToString(" / ") { "${it.className} ${it.level}" }

        // Classe primária (para compatibilidade com regras gerais)
        val primaryClassInfo = Dnd35Rules.CLASSES.find { it.name.equals(classEntries.first().className, ignoreCase = true) }
            ?: Dnd35Rules.CLASSES.first()

        // Determinar classe favorecida (para Humanos e Meio-Elfos é a classe de nível mais alto)
        val effectiveFavoredClassName = if (race.favoredClass.equals("Qualquer", ignoreCase = true) ||
            race.name.equals("Humano", ignoreCase = true) ||
            race.name.equals("Meio-Elfo", ignoreCase = true)
        ) {
            classEntries.maxByOrNull { it.level }?.className ?: race.favoredClass
        } else {
            race.favoredClass
        }

        // 2. Construir detalhes acumulados por classe
        val multiclassDetails = classEntries.map { entry ->
            val info = Dnd35Rules.CLASSES.find { it.name.equals(entry.className, ignoreCase = true) }
                ?: Dnd35Rules.CLASSES.first()
            val babPart = Dnd35Rules.calculateBaseAttackBonus(info.babType, entry.level)
            val fortPart = Dnd35Rules.calculateBaseSave(info.fortType, entry.level)
            val refPart = Dnd35Rules.calculateBaseSave(info.refType, entry.level)
            val willPart = Dnd35Rules.calculateBaseSave(info.willType, entry.level)
            val isFavored = entry.className.equals(effectiveFavoredClassName, ignoreCase = true)

            ClassDetailInfo(
                entry = entry,
                classInfo = info,
                babContribution = babPart,
                baseFort = fortPart,
                baseRef = refPart,
                baseWill = willPart,
                hitDie = info.hitDie,
                isCaster = info.spellCastingAbility != null,
                isFavored = isFavored
            )
        }

        // 3. Calcular penalidade de XP de Multiclasse segundo D&D 3.5
        val multiclassPenalty = calculateMulticlassPenalty(classEntries, race, effectiveFavoredClassName)

        val skills = CharacterJsonAdapters.deserializeSkills(character.skillsJson).ifEmpty {
            defaultSkillsForClasses(multiclassDetails.map { it.classInfo })
        }
        val inventory = CharacterJsonAdapters.deserializeInventory(character.inventoryJson)
        val feats = CharacterJsonAdapters.deserializeFeats(character.featsJson)
        val spells = CharacterJsonAdapters.deserializeSpells(character.spellsJson)
        val spellSlots = CharacterJsonAdapters.deserializeSpellSlots(character.spellSlotsJson)

        // 4. Processar bônus concedidos por itens equipados nos espaços de corpo (EquipmentSlots)
        var itemStrBonus = character.modStrBonus
        var itemDexBonus = character.modDexBonus
        var itemConBonus = character.modConBonus
        var itemIntBonus = character.modIntBonus
        var itemWisBonus = character.modWisBonus
        var itemChaBonus = character.modChaBonus

        var itemArmorBonus = character.armorBonus
        var itemShieldBonus = character.shieldBonus
        var itemNaturalArmor = character.naturalArmor
        var itemDeflectionBonus = character.deflectionBonus
        var itemMiscSaveBonus = 0
        var itemMiscAcBonus = character.miscAcBonus

        var armorMaxDex: Int? = null
        var totalArmorCheckPenalty = 0

        for (item in inventory) {
            val isEquipped = item.equipped
            if (!isEquipped) continue

            val slot = item.equippedSlot
            val props = "${item.damageOrAc} ${item.notes}"

            // Armadura no Corpo
            if (slot == Dnd35Rules.EquipmentSlots.ARMOR_BODY || item.category.equals("Armaduras", ignoreCase = true)) {
                val ac = parseBonusNumber(item.damageOrAc)
                if (ac > 0) itemArmorBonus = maxOf(itemArmorBonus, ac)
                val mDex = parseMaxDex(item.notes)
                if (mDex != null) {
                    armorMaxDex = if (armorMaxDex == null) mDex else minOf(armorMaxDex, mDex)
                }
                totalArmorCheckPenalty += parsePenalty(item.notes)
            }

            // Escudo na Mão Secundária
            if (slot == Dnd35Rules.EquipmentSlots.OFF_HAND && item.category.equals("Escudos", ignoreCase = true)) {
                val ac = parseBonusNumber(item.damageOrAc)
                if (ac > 0) itemShieldBonus = maxOf(itemShieldBonus, ac)
                totalArmorCheckPenalty += parsePenalty(item.notes)
            }

            // Itens de Cabeça, Pescoço, Ombros, Mãos, etc.
            parseSpecificBonus(props, "FOR").takeIf { it > 0 }?.let { itemStrBonus = maxOf(itemStrBonus, it) }
            parseSpecificBonus(props, "DES").takeIf { it > 0 }?.let { itemDexBonus = maxOf(itemDexBonus, it) }
            parseSpecificBonus(props, "CON").takeIf { it > 0 }?.let { itemConBonus = maxOf(itemConBonus, it) }
            parseSpecificBonus(props, "INT").takeIf { it > 0 }?.let { itemIntBonus = maxOf(itemIntBonus, it) }
            parseSpecificBonus(props, "SAB").takeIf { it > 0 }?.let { itemWisBonus = maxOf(itemWisBonus, it) }
            parseSpecificBonus(props, "CAR").takeIf { it > 0 }?.let { itemChaBonus = maxOf(itemChaBonus, it) }

            if (props.contains("Deflexão", ignoreCase = true) || slot in listOf(Dnd35Rules.EquipmentSlots.RING_1, Dnd35Rules.EquipmentSlots.RING_2)) {
                val defl = parseBonusNumber(item.damageOrAc)
                if (defl > 0) itemDeflectionBonus = maxOf(itemDeflectionBonus, defl)
            }
            if (props.contains("Natural", ignoreCase = true) || (slot == Dnd35Rules.EquipmentSlots.NECK && props.contains("Armadura Natural"))) {
                val nat = parseBonusNumber(item.damageOrAc)
                if (nat > 0) itemNaturalArmor = maxOf(itemNaturalArmor, nat)
            }
            if (props.contains("Resistências", ignoreCase = true) || (slot == Dnd35Rules.EquipmentSlots.SHOULDERS && props.contains("+1"))) {
                itemMiscSaveBonus = maxOf(itemMiscSaveBonus, parseBonusNumber(item.damageOrAc))
            }
        }

        // 5. Atributos Finais
        val totalStr = character.baseStr + race.strMod + itemStrBonus
        val strMod = Dnd35Rules.calculateModifier(totalStr)

        val totalDex = character.baseDex + race.dexMod + itemDexBonus
        val dexMod = Dnd35Rules.calculateModifier(totalDex)

        val totalCon = character.baseCon + race.conMod + itemConBonus
        val conMod = Dnd35Rules.calculateModifier(totalCon)

        val totalInt = character.baseInt + race.intMod + itemIntBonus
        val intMod = Dnd35Rules.calculateModifier(totalInt)

        val totalWis = character.baseWis + race.wisMod + itemWisBonus
        val wisMod = Dnd35Rules.calculateModifier(totalWis)

        val totalCha = character.baseCha + race.chaMod + itemChaBonus
        val chaMod = Dnd35Rules.calculateModifier(totalCha)

        // 6. BBA Cumulativo de todas as classes & Agarrar
        val bab = multiclassDetails.sumOf { it.babContribution }
        val babDisplay = formatBabProgression(bab)
        val sizeGrappleMod = Dnd35Rules.getGrappleSizeModifier(character.size)
        val grappleBonus = bab + strMod + sizeGrappleMod

        // 7. Testes de Resistência Cumulativos das Classes
        val baseFort = multiclassDetails.sumOf { it.baseFort }
        val baseRef = multiclassDetails.sumOf { it.baseRef }
        val baseWill = multiclassDetails.sumOf { it.baseWill }

        // Regra de Paladino: Graça Divina (Divine Grace) no 2º nível soma mod Carisma a todos os testes
        val paladinLevel = classEntries.find { it.className.equals("Paladino", ignoreCase = true) }?.level ?: 0
        val paladinDivineGrace = if (paladinLevel >= 2) {
            maxOf(0, chaMod)
        } else 0

        // Halfling Robusto (Coração-Forte) não recebe o +1 racial geral nas resistências.
        val halflingSaveBonus = if (race.name.equals("Halfling", ignoreCase = true)) 1 else 0

        val featFort = if (feats.any { it.name.contains("Fortitude Maior", ignoreCase = true) }) 2 else 0
        val featRef = if (feats.any { it.name.contains("Reflexos Rápidos", ignoreCase = true) }) 2 else 0
        val featWill = if (feats.any { it.name.contains("Vontade de Ferro", ignoreCase = true) }) 2 else 0

        val totalFort = baseFort + conMod + character.miscFortitude + itemMiscSaveBonus + paladinDivineGrace + halflingSaveBonus + featFort
        val totalRef = baseRef + dexMod + character.miscReflex + itemMiscSaveBonus + paladinDivineGrace + halflingSaveBonus + featRef
        val totalWill = baseWill + wisMod + character.miscWill + itemMiscSaveBonus + paladinDivineGrace + halflingSaveBonus + featWill

        // 8. Peso e Gestão de Carga
        val coinWeight = (character.goldGp + character.silverSp + character.copperCp + character.platinumPp) / 50.0f
        var itemsWeight = 0.0f
        for (item in inventory) {
            itemsWeight += (item.weightLb * item.quantity)
        }
        val totalWeightLb = itemsWeight + coinWeight
        val carryingCapacity = Dnd35Rules.getCarryingCapacity(totalStr, character.size)
        val encumbrance = Dnd35Rules.calculateEncumbrance(totalWeightLb, totalStr, character.size)

        // Limite de DES imposto pela armadura e pela carga
        val combinedMaxDex = when {
            armorMaxDex != null && encumbrance.maxDexBonus != null -> minOf(armorMaxDex, encumbrance.maxDexBonus)
            armorMaxDex != null -> armorMaxDex
            encumbrance.maxDexBonus != null -> encumbrance.maxDexBonus
            else -> null
        }

        val effectiveDexForAc = if (combinedMaxDex != null) minOf(dexMod, combinedMaxDex) else dexMod

        // Bônus de CA sem armadura do Monge (usa nível de Monge da multiclasse)
        val monkLevel = classEntries.find { it.className.equals("Monge", ignoreCase = true) }?.level ?: 0
        val isWearingArmor = itemArmorBonus > 0 || itemShieldBonus > 0
        val monkAcBonus = if (
            monkLevel > 0 && !isWearingArmor &&
            encumbrance == Dnd35Rules.EncumbranceLevel.LIGHT
        ) {
            maxOf(0, wisMod) + (monkLevel / 5)
        } else 0

        // A penalidade de carga não se soma à penalidade da armadura: usa-se a pior das duas.
        // A penalidade do escudo já está incluída em totalArmorCheckPenalty.
        val combinedArmorCheckPenalty = minOf(totalArmorCheckPenalty, encumbrance.armorCheckPenalty)

        val acSizeMod = Dnd35Rules.getArmorClassSizeModifier(character.size)

        val totalAc = 10 + itemArmorBonus + itemShieldBonus + effectiveDexForAc +
                itemNaturalArmor + itemDeflectionBonus + itemMiscAcBonus + monkAcBonus + acSizeMod

        val touchAc = 10 + effectiveDexForAc + itemDeflectionBonus + itemMiscAcBonus + monkAcBonus + acSizeMod

        val flatFootedAc = 10 + itemArmorBonus + itemShieldBonus + minOf(0, effectiveDexForAc) +
                itemNaturalArmor + itemDeflectionBonus + itemMiscAcBonus + monkAcBonus + acSizeMod

        // 9. Iniciativa & Velocidade
        val featInit = if (feats.any { it.name.contains("Iniciativa Aprimorada", ignoreCase = true) }) 4 else 0
        val initiativeTotal = dexMod + character.miscInitiative + featInit

        // O valor legado padrão de 9m não pode substituir o deslocamento racial (ex.: anão/halfling).
        var baseSpeed = if (
            character.baseSpeedMeters <= 0f ||
            (character.baseSpeedMeters == 9.0f && race.baseSpeedMeters != 9.0f)
        ) race.baseSpeedMeters else character.baseSpeedMeters
        val barbarianLevel = classEntries.find { it.className.equals("Bárbaro", ignoreCase = true) }?.level ?: 0
        val equippedArmor = inventory.filter {
            it.equipped && (it.equippedSlot == Dnd35Rules.EquipmentSlots.ARMOR_BODY ||
                it.category.equals("Armaduras", ignoreCase = true))
        }
        // Movimento rápido do Bárbaro só funciona sem armadura média/pesada e com carga leve.
        if (barbarianLevel >= 1 && equippedArmor.none { isMediumOrHeavyArmor(it.name) } &&
            encumbrance == Dnd35Rules.EncumbranceLevel.LIGHT) {
            baseSpeed += 3.0f
        }
        // Movimento rápido do Monge exige ausência de armadura/escudo e carga leve.
        if (monkLevel > 0 && !isWearingArmor &&
            encumbrance == Dnd35Rules.EncumbranceLevel.LIGHT) {
            val monkSpeedBonus = (monkLevel / 3) * 3.0f
            baseSpeed += monkSpeedBonus
        }

        // Armadura e carga não acumulam reduções de deslocamento: aplica-se apenas a pior.
        // Anões mantêm deslocamento terrestre de 6m mesmo com armadura/carga média ou pesada.
        val isDwarf = race.name.equals("Anão", ignoreCase = true) ||
            race.name.equals("Anão da Montanha", ignoreCase = true)
        val hasMediumOrHeavyArmor = equippedArmor.any { isMediumOrHeavyArmor(it.name) }
        val hasMediumOrHeavyLoad = encumbrance != Dnd35Rules.EncumbranceLevel.LIGHT
        val speedMetersEffective = when {
            encumbrance == Dnd35Rules.EncumbranceLevel.OVERLOADED -> 1.5f
            isDwarf -> baseSpeed
            !hasMediumOrHeavyArmor && !hasMediumOrHeavyLoad -> baseSpeed
            else -> reduceSpeedForArmorOrLoad(baseSpeed)
        }

        // 10. Habilidades Especiais Cumulativas de todas as Classes
        val activeClassFeatures = mutableListOf<Dnd35Rules.ClassFeature>()
        for (detail in multiclassDetails) {
            activeClassFeatures.addAll(detail.classInfo.features.filter { it.level <= detail.entry.level })
        }

        // Ataque Furtivo (usa nível de Ladino)
        val rogueLevel = classEntries.find { it.className.equals("Ladino", ignoreCase = true) }?.level ?: 0
        val sneakAttackDice = if (rogueLevel > 0) {
            val diceCount = (rogueLevel + 1) / 2
            "+${diceCount}d6"
        } else null

        // Dano desarmado do Monge: progressão por nível e ajuste de tamanho.
        val monkUnarmedDamage = if (monkLevel > 0) {
            monkUnarmedDamageForSize(monkLevel, character.size)
        } else null

        // Fúria de Bárbaro
        val rageUsesPerDay = if (barbarianLevel > 0) {
            when {
                barbarianLevel >= 20 -> 6
                barbarianLevel >= 16 -> 5
                barbarianLevel >= 12 -> 4
                barbarianLevel >= 8 -> 3
                barbarianLevel >= 4 -> 2
                else -> 1
            }
        } else null

        // Expulsar Mortos-Vivos (Clérigo nv 1+ ou Paladino nv 4+)
        val clericLevel = classEntries.find { it.className.equals("Clérigo", ignoreCase = true) }?.level ?: 0
        val hasTurning = clericLevel >= 1 || paladinLevel >= 4
        val turningUsesPerDay = if (hasTurning) {
            maxOf(0, 3 + chaMod)
        } else null

        // 11. Classes Conjuradoras da Multiclasse & CDs de Magia
        val casterClasses = multiclassDetails
            .filter { detail ->
                val classLevel = detail.entry.level
                val hasReachedSpellcastingLevel = when (detail.entry.className.lowercase()) {
                    "paladino", "ranger" -> classLevel >= 4
                    else -> true
                }
                detail.classInfo.spellCastingAbility != null && hasReachedSpellcastingLevel
            }
            .map { detail ->
                val ability = detail.classInfo.spellCastingAbility!!
                val mod = when (ability) {
                    "INT" -> intMod
                    "SAB" -> wisMod
                    "CAR" -> chaMod
                    else -> 0
                }
                val className = detail.entry.className
                val casterLevel = SpellcastingRules.casterLevel(className, detail.entry.level)
                val dcMap = when (className.lowercase()) {
                    "paladino", "ranger" -> {
                        val maxSpellLevel = when {
                            detail.entry.level >= 14 -> 4
                            detail.entry.level >= 11 -> 3
                            detail.entry.level >= 8 -> 2
                            else -> 1
                        }
                        (1..maxSpellLevel).associateWith { lvl -> 10 + lvl + mod }
                    }
                    else -> (0..9).associateWith { lvl -> 10 + lvl + mod }
                }
                CasterClassInfo(
                    className = className,
                    casterLevel = casterLevel,
                    keyAbility = ability,
                    keyAbilityMod = mod,
                    dcByLevel = dcMap
                )
            }

        val isSpellcaster = casterClasses.isNotEmpty()
        val primaryCaster = casterClasses.firstOrNull()
        val castingAbilityName = primaryCaster?.keyAbility
        val castingAbilityMod = primaryCaster?.keyAbilityMod ?: 0

        val spellSaveDcByLevel = primaryCaster?.dcByLevel ?: emptyMap()

        // 12. Perícias Calculadas (Regra 3.5: Perícia de classe se pertencer a QUALQUER uma das classes)
        val computedSkills = skills.map { skill ->
            val canonicalSkillName = Dnd35Rules.canonicalSkillName(skill.name)
            val def = Dnd35Rules.SKILL_DEFINITIONS.find { it.name.equals(canonicalSkillName, ignoreCase = true) }
            // Para perícias do catálogo, o atributo-chave oficial prevalece sobre dados legados/editáveis.
            // Perícias personalizadas continuam usando o atributo escolhido na ficha.
            val keyAbility = Dnd35Rules.canonicalSkillKeyAbility(skill.name, skill.keyAbility)
            val keyMod = when (keyAbility.uppercase()) {
                "FOR" -> strMod
                "DES" -> dexMod
                "CON" -> conMod
                "INT" -> intMod
                "SAB" -> wisMod
                "CAR" -> chaMod
                else -> 0
            }
            // Perícias conhecidas seguem a lista oficial de perícias de classe de qualquer classe
            // do personagem. O sinalizador salvo só serve para perícias personalizadas.
            val isClassSkill = if (def != null || Dnd35Rules.isCraftSpecialty(canonicalSkillName)) {
                multiclassDetails.any { detail ->
                    detail.classInfo.classSkills.any {
                        Dnd35Rules.classSkillMatches(canonicalSkillName, it)
                    }
                }
            } else skill.isClassSkill
            val maxClassRanks = totalCharacterLevel + 3f
            val maxRanks = if (isClassSkill) maxClassRanks else maxClassRanks / 2f
            // Mantém incrementos de 0,5 mesmo quando a perícia se torna de classe
            // por multiclasse: graduações parciais adquiridas anteriormente não podem
            // ser arredondadas para cima ou descartadas. Meias graduações não aumentam
            // o teste até que completem uma graduação inteira (ver totalBonus abaixo).
            val normalizedRanks =
                (skill.ranks.coerceAtLeast(0f) * 2f).roundToInt() / 2f
            val ranks = normalizedRanks.coerceAtMost(maxRanks)
            val racialBonus = racialSkillBonus(race.name, canonicalSkillName)
            val sizeBonus = sizeSkillBonus(character.size, canonicalSkillName)
            val hasArmorPenalty = def?.armorCheckPenaltyApplies ?: false
            val appliedPenalty = if (hasArmorPenalty) {
                if (canonicalSkillName.equals("Natação", ignoreCase = true)) {
                    combinedArmorCheckPenalty * 2
                } else {
                    combinedArmorCheckPenalty
                }
            } else 0

            val total = ranks.toInt() + keyMod + racialBonus + sizeBonus + skill.miscBonus + appliedPenalty
            ComputedSkill(
                skill = skill.copy(name = def?.name ?: skill.name, keyAbility = keyAbility, ranks = ranks, isClassSkill = isClassSkill),
                keyAbilityMod = keyMod,
                ranks = ranks,
                racialBonus = racialBonus,
                sizeBonus = sizeBonus,
                miscBonus = skill.miscBonus,
                armorPenaltyApplied = appliedPenalty,
                totalBonus = total,
                isClassSkill = isClassSkill,
                maxRanks = maxRanks,
                trainedOnly = def?.trainedOnly ?: false,
                canRoll = !(def?.trainedOnly ?: false) || ranks >= 1f ||
                    Dnd35Rules.allowsUntrainedKnowledgeCheck(canonicalSkillName)
            )
        }

        return CalculatedCharacterStats(
            character = character,
            raceInfo = race,
            classInfo = primaryClassInfo,
            classEntries = classEntries,
            multiclassDetails = multiclassDetails,
            multiclassPenalty = multiclassPenalty,
            isMulticlass = isMulticlass,
            classesDisplay = classesDisplay,
            totalCharacterLevel = totalCharacterLevel,
            skills = skills,
            inventory = inventory,
            feats = feats,
            spells = spells,
            spellSlots = spellSlots,
            totalStr = totalStr,
            strMod = strMod,
            totalDex = totalDex,
            dexMod = dexMod,
            totalCon = totalCon,
            conMod = conMod,
            totalInt = totalInt,
            intMod = intMod,
            totalWis = totalWis,
            wisMod = wisMod,
            totalCha = totalCha,
            chaMod = chaMod,
            bab = bab,
            babDisplay = babDisplay,
            grappleBonus = grappleBonus,
            baseFort = baseFort,
            totalFort = totalFort,
            baseRef = baseRef,
            totalRef = totalRef,
            baseWill = baseWill,
            totalWill = totalWill,
            initiativeTotal = initiativeTotal,
            speedMetersEffective = speedMetersEffective,
            armorBonusTotal = itemArmorBonus,
            shieldBonusTotal = itemShieldBonus,
            naturalArmorTotal = itemNaturalArmor,
            deflectionBonusTotal = itemDeflectionBonus,
            maxDexFromArmor = combinedMaxDex,
            effectiveDexForAc = effectiveDexForAc,
            armorCheckPenalty = combinedArmorCheckPenalty,
            totalAc = totalAc,
            touchAc = touchAc,
            flatFootedAc = flatFootedAc,
            activeClassFeatures = activeClassFeatures,
            sneakAttackDice = sneakAttackDice,
            monkUnarmedDamage = monkUnarmedDamage,
            rageUsesPerDay = rageUsesPerDay,
            turningUsesPerDay = turningUsesPerDay,
            isSpellcaster = isSpellcaster,
            castingAbilityName = castingAbilityName,
            castingAbilityMod = castingAbilityMod,
            spellSaveDcByLevel = spellSaveDcByLevel,
            casterClasses = casterClasses,
            totalWeightLb = totalWeightLb,
            carryingCapacity = carryingCapacity,
            encumbrance = encumbrance,
            computedSkills = computedSkills
        )
    }

    private fun calculateMulticlassPenalty(
        classes: List<CharacterClassEntry>,
        race: Dnd35Rules.RaceInfo,
        favoredClassName: String
    ): MulticlassPenaltyInfo {
        if (classes.size <= 1) {
            return MulticlassPenaltyInfo(
                hasPenalty = false,
                penaltyPercent = 0,
                favoredClass = favoredClassName,
                explanation = "Classe única. Nenhuma penalidade de XP aplicada."
            )
        }

        // Filtra a classe favorecida (não conta para a disparidade de XP de acordo com o Livro do Jogador)
        val nonFavored = classes.filterNot { it.className.equals(favoredClassName, ignoreCase = true) }

        if (nonFavored.size <= 1) {
            return MulticlassPenaltyInfo(
                hasPenalty = false,
                penaltyPercent = 0,
                favoredClass = favoredClassName,
                explanation = "Sem penalidade de XP. Apenas 1 classe não favorecida ou todas em harmonia."
            )
        }

        val maxNonFavoredLevel = nonFavored.maxOf { it.level }
        val offending = nonFavored.filter { (maxNonFavoredLevel - it.level) >= 2 }

        return if (offending.isNotEmpty()) {
            val penalty = offending.size * 20
            MulticlassPenaltyInfo(
                hasPenalty = true,
                penaltyPercent = penalty,
                favoredClass = favoredClassName,
                explanation = "-${penalty}% XP: Diferença de 2 ou mais níveis entre classes não favorecidas (${offending.joinToString { "${it.className} (${it.level})" }}). Favorecida: $favoredClassName."
            )
        } else {
            MulticlassPenaltyInfo(
                hasPenalty = false,
                penaltyPercent = 0,
                favoredClass = favoredClassName,
                explanation = "Sem penalidade de XP: Classes não favorecidas equilibradas (diferença menor que 2 níveis). Favorecida: $favoredClassName."
            )
        }
    }

    fun defaultSkillsForClasses(classes: List<Dnd35Rules.ClassInfo>): List<CharacterSkill> {
        return Dnd35Rules.SKILL_DEFINITIONS.map { def ->
            val canonicalName = Dnd35Rules.canonicalSkillName(def.name)
            val isClass = classes.any { c -> c.classSkills.any { it.equals(canonicalName, ignoreCase = true) } }
            CharacterSkill(
                name = def.name,
                keyAbility = def.keyAbility,
                ranks = 0.0f,
                miscBonus = 0,
                isClassSkill = isClass
            )
        }
    }

    fun defaultSkillsForClass(classInfo: Dnd35Rules.ClassInfo): List<CharacterSkill> {
        return defaultSkillsForClasses(listOf(classInfo))
    }

    private fun isMediumOrHeavyArmor(name: String): Boolean {
        val normalized = name.trim().lowercase()
        val mediumOrHeavyArmor = listOf(
            "gibão de peles", "gibao de peles", "brunea", "cota de malha",
            "peitoral de aço", "peitoral de aco", "cota de talas", "cota de bandas",
            "armadura de meia-placa", "armadura de meia placa", "armadura completa",
            "armadura de escamas", "armadura de placas e malha", "splint mail",
            "banded mail", "half plate", "full plate"
        )
        return mediumOrHeavyArmor.any { normalized.contains(it) }
    }

    private fun isHeavyArmor(name: String): Boolean {
        val normalized = name.trim().lowercase()
        val heavyArmor = listOf(
            "cota de talas", "cota de bandas", "armadura de meia-placa",
            "armadura de meia placa", "armadura completa", "armadura de placas e malha",
            "splint mail", "banded mail", "half plate", "full plate"
        )
        return heavyArmor.any { normalized.contains(it) }
    }

    private fun reduceSpeedForArmorOrLoad(speedMeters: Float): Float {
        val speedFeet = (speedMeters / 0.3f).roundToInt()
        val reducedFeet = when (speedFeet) {
            20 -> 15
            30 -> 20
            40 -> 30
            50 -> 35
            60 -> 40
            70 -> 50
            80 -> 55
            90 -> 60
            100 -> 70
            else -> if (speedFeet < 20) {
                (speedFeet - 5).coerceAtLeast(5)
            } else {
                ((speedFeet * 2 / 3) / 5) * 5
            }
        }
        return reducedFeet * 0.3f
    }

    private fun formatBabProgression(bab: Int): String {
        if (bab <= 0) return "+0"
        val attacks = mutableListOf<String>()
        var current = bab
        while (current > 0) {
            attacks.add("+$current")
            current -= 5
        }
        return attacks.joinToString("/")
    }

    private fun parseBonusNumber(text: String): Int {
        val match = Regex("""\+?(\d+)""").find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun parseSpecificBonus(text: String, ability: String): Int {
        // Aceita bônus em qualquer posição da descrição (ex.: "+6 FOR; item mágico").
        // Retorna zero quando o atributo não aparece, sem inventar um bônus padrão.
        val match = Regex("""\+(\d+)\s*$ability\b""", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun parseMaxDex(text: String): Int? {
        val match = Regex("""Des\s*Max\s*\+?(\d+)""", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun parsePenalty(text: String): Int {
        val match = Regex("""Pen\s*(-?\d+)""", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }
}


/** Regras comuns de conjuração do Livro do Jogador; progressões por classe ficam separadas. */
object SpellcastingRules {
    enum class Method { NONE, SPONTANEOUS_KNOWN_SPELLS, SPELLBOOK_PREPARATION, DIVINE_PREPARATION }

    fun methodForClass(name: String): Method = when (name.trim().lowercase()) {
        "bardo", "feiticeiro" -> Method.SPONTANEOUS_KNOWN_SPELLS
        "mago" -> Method.SPELLBOOK_PREPARATION
        "clérigo", "clerigo", "druida", "paladino", "ranger" -> Method.DIVINE_PREPARATION
        else -> Method.NONE
    }

    fun bonusSlots(abilityScore: Int, spellLevel: Int): Int {
        if (spellLevel !in 1..9) return 0
        val modifier = Dnd35Rules.calculateModifier(abilityScore)
        return if (modifier < spellLevel) 0 else 1 + (modifier - spellLevel) / 4
    }

    fun meetsAbilityRequirement(abilityScore: Int, spellLevel: Int): Boolean =
        spellLevel in 0..9 && abilityScore >= 10 + spellLevel

    fun casterLevel(className: String, classLevel: Int): Int {
        val level = classLevel.coerceAtLeast(0)
        return when (className.trim().lowercase()) {
            "paladino" -> if (level >= 4) level - 3 else 0
            "ranger" -> if (level >= 4) level / 2 else 0
            else -> level
        }
    }


    // Tabelas 3-5 e 3-10 do Livro do Jogador brasileiro:
    // quantidade fixa de magias conhecidas, independente dos espaços adicionais por atributo.
    private val bardKnown = arrayOf(
        intArrayOf(4, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 2, 0, 0, 0, 0, 0),
        intArrayOf(6, 3, 0, 0, 0, 0, 0),
        intArrayOf(6, 3, 2, 0, 0, 0, 0),
        intArrayOf(6, 4, 3, 0, 0, 0, 0),
        intArrayOf(6, 4, 3, 0, 0, 0, 0),
        intArrayOf(6, 4, 4, 2, 0, 0, 0),
        intArrayOf(6, 4, 4, 3, 0, 0, 0),
        intArrayOf(6, 4, 4, 3, 0, 0, 0),
        intArrayOf(6, 4, 4, 4, 2, 0, 0),
        intArrayOf(6, 4, 4, 4, 3, 0, 0),
        intArrayOf(6, 4, 4, 4, 3, 0, 0),
        intArrayOf(6, 4, 4, 4, 4, 2, 0),
        intArrayOf(6, 4, 4, 4, 4, 3, 0),
        intArrayOf(6, 4, 4, 4, 4, 3, 0),
        intArrayOf(6, 5, 4, 4, 4, 4, 2),
        intArrayOf(6, 5, 5, 4, 4, 4, 3),
        intArrayOf(6, 5, 5, 5, 4, 4, 3),
        intArrayOf(6, 5, 5, 5, 5, 4, 4),
        intArrayOf(6, 5, 5, 5, 5, 5, 4)
    )

    private val sorcererKnown = arrayOf(
        intArrayOf(4, 2, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 2, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 3, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 3, 1, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 4, 2, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(7, 4, 2, 1, 0, 0, 0, 0, 0, 0),
        intArrayOf(7, 5, 3, 2, 0, 0, 0, 0, 0, 0),
        intArrayOf(8, 5, 3, 2, 1, 0, 0, 0, 0, 0),
        intArrayOf(8, 5, 4, 3, 2, 0, 0, 0, 0, 0),
        intArrayOf(9, 5, 4, 3, 2, 1, 0, 0, 0, 0),
        intArrayOf(9, 5, 5, 4, 3, 2, 0, 0, 0, 0),
        intArrayOf(9, 5, 5, 4, 3, 2, 1, 0, 0, 0),
        intArrayOf(9, 5, 5, 4, 4, 3, 2, 0, 0, 0),
        intArrayOf(9, 5, 5, 4, 4, 3, 2, 1, 0, 0),
        intArrayOf(9, 5, 5, 4, 4, 4, 3, 2, 0, 0),
        intArrayOf(9, 5, 5, 4, 4, 4, 3, 2, 1, 0),
        intArrayOf(9, 5, 5, 4, 4, 4, 3, 3, 2, 0),
        intArrayOf(9, 5, 5, 4, 4, 4, 3, 3, 2, 1),
        intArrayOf(9, 5, 5, 4, 4, 4, 3, 3, 3, 2),
        intArrayOf(9, 5, 5, 4, 4, 4, 3, 3, 3, 3)
    )

    // Tabelas 3-4 (Bardo) e 3-9 (Feiticeiro): espaços-base por dia.
    // Os espaços adicionais por atributo são calculados separadamente.
    private val bardDaily = arrayOf(
        intArrayOf(2, 0, 0, 0, 0, 0, 0),
        intArrayOf(3, 0, 0, 0, 0, 0, 0),
        intArrayOf(3, 1, 0, 0, 0, 0, 0),
        intArrayOf(3, 2, 0, 0, 0, 0, 0),
        intArrayOf(3, 3, 1, 0, 0, 0, 0),
        intArrayOf(3, 3, 2, 0, 0, 0, 0),
        intArrayOf(3, 3, 2, 0, 0, 0, 0),
        intArrayOf(3, 3, 3, 1, 0, 0, 0),
        intArrayOf(3, 3, 3, 2, 0, 0, 0),
        intArrayOf(3, 3, 3, 2, 0, 0, 0),
        intArrayOf(3, 3, 3, 3, 1, 0, 0),
        intArrayOf(3, 3, 3, 3, 2, 0, 0),
        intArrayOf(3, 3, 3, 3, 2, 0, 0),
        intArrayOf(4, 3, 3, 3, 3, 1, 0),
        intArrayOf(4, 4, 3, 3, 3, 2, 0),
        intArrayOf(4, 4, 4, 3, 3, 2, 0),
        intArrayOf(4, 4, 4, 4, 3, 3, 1),
        intArrayOf(4, 4, 4, 4, 4, 3, 2),
        intArrayOf(4, 4, 4, 4, 4, 4, 3),
        intArrayOf(4, 4, 4, 4, 4, 4, 4)
    )

    // Tabela 3-8 (Druida), Livro do Jogador brasileiro: espaços-base por dia.
    private val druidDaily = arrayOf(
        intArrayOf(3, 1, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(4, 2, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(4, 2, 1, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 3, 2, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 3, 2, 1, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 3, 3, 2, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 4, 3, 2, 1, 0, 0, 0, 0, 0),
        intArrayOf(6, 4, 3, 3, 2, 0, 0, 0, 0, 0),
        intArrayOf(6, 4, 4, 3, 2, 1, 0, 0, 0, 0),
        intArrayOf(6, 4, 4, 3, 3, 2, 0, 0, 0, 0),
        intArrayOf(6, 5, 4, 4, 3, 2, 1, 0, 0, 0),
        intArrayOf(6, 5, 4, 4, 3, 3, 2, 0, 0, 0),
        intArrayOf(6, 5, 5, 4, 4, 3, 2, 1, 0, 0),
        intArrayOf(6, 5, 5, 4, 4, 3, 3, 2, 0, 0),
        intArrayOf(6, 5, 5, 5, 4, 4, 3, 2, 1, 0),
        intArrayOf(6, 5, 5, 5, 4, 4, 3, 3, 2, 0),
        intArrayOf(6, 5, 5, 5, 5, 4, 4, 3, 2, 1),
        intArrayOf(6, 5, 5, 5, 5, 4, 4, 3, 3, 2),
        intArrayOf(6, 5, 5, 5, 5, 5, 4, 4, 3, 3),
        intArrayOf(6, 5, 5, 5, 5, 5, 4, 4, 4, 4)
    )

    private val sorcererDaily = arrayOf(
        intArrayOf(5, 3, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 4, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 5, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 3, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 4, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 5, 3, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 4, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 5, 3, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 6, 4, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 6, 5, 3, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 6, 6, 4, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 6, 6, 5, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 6, 6, 6, 5, 3, 0, 0),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 4, 0, 0),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 5, 3, 0),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 4, 0),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 5, 3),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 6, 4),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 6, 6)
    )

    /** Espaços-base por dia, sem bônus de atributo; null indica tabela ainda não implementada. */
    fun baseDailySlots(className: String, classLevel: Int, spellLevel: Int): Int? {
        if (classLevel !in 1..20 || spellLevel !in 0..9) return null
        val table = when (className.trim().lowercase()) {
            "bardo" -> bardDaily
            "clérigo" -> druidDaily // Tabela 3-6: mesma progressão-base diária do Druida.
            "druida" -> druidDaily
            "feiticeiro" -> sorcererDaily
            else -> return null
        }
        val row = table[classLevel - 1]
        return if (spellLevel < row.size) row[spellLevel] else 0
    }

    /**
     * Espaço adicional de domínio do Clérigo. É separado dos espaços-base e dos bônus
     * por atributo; círculos 0 não recebem espaço de domínio.
     * Retorna null para níveis/círculos inválidos ou classes sem domínio.
     */
    fun domainSlots(className: String, classLevel: Int, spellLevel: Int): Int? {
        if (classLevel !in 1..20 || spellLevel !in 0..9) return null
        if (className.trim().lowercase() != "clérigo") return null
        if (spellLevel == 0) return 0
        return if ((baseDailySlots("Clérigo", classLevel, spellLevel) ?: 0) > 0) 1 else 0
    }

    /** Retorna a quantidade da tabela para o nível da classe e círculo indicados. */
    fun knownSpells(className: String, classLevel: Int, spellLevel: Int): Int? {
        if (classLevel !in 1..20 || spellLevel !in 0..9) return null
        val table = when (className.trim().lowercase()) {
            "bardo" -> bardKnown
            "feiticeiro" -> sorcererKnown
            else -> return null
        }
        val row = table[classLevel - 1]
        return if (spellLevel < row.size) row[spellLevel] else 0
    }
}
