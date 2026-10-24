package com.example.dnd35.data

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSkill
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellSlotInfo
import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val characterDao: CharacterDao) {

    val allCharacters: Flow<List<CharacterEntity>> = characterDao.getAllCharacters()

    fun getCharacter(id: Long): Flow<CharacterEntity?> = characterDao.getCharacterById(id)

    suspend fun saveCharacter(character: CharacterEntity): Long {
        val updated = character.copy(updatedAt = System.currentTimeMillis())
        return if (character.id == 0L) {
            characterDao.insertCharacter(updated)
        } else {
            characterDao.updateCharacter(updated)
            character.id
        }
    }

    suspend fun createNewCharacter(name: String, characterClass: String, race: String): Long {
        val classInfo = Dnd35Rules.CLASSES.find { it.name.equals(characterClass, ignoreCase = true) }
            ?: Dnd35Rules.CLASSES.first()
        val raceInfo = Dnd35Rules.RACES.find { it.name.equals(race, ignoreCase = true) }
            ?: Dnd35Rules.RACES.first()

        val initialSkills = Dnd35Rules.SKILL_DEFINITIONS.map { def ->
            val isClass = classInfo.classSkills.any { it.equals(def.name, ignoreCase = true) }
            CharacterSkill(
                name = def.name,
                keyAbility = def.keyAbility,
                ranks = 0.0f,
                miscBonus = 0,
                isClassSkill = isClass
            )
        }

        val baseHp = classInfo.hitDie + Dnd35Rules.calculateModifier(14 + raceInfo.conMod)
        val initialSlots = (0..9).map { lvl ->
            SpellSlotInfo(level = lvl, totalSlots = if (lvl <= 1) 3 else 0, usedSlots = 0)
        }

        val newChar = CharacterEntity(
            name = name.ifBlank { "Novo Herói" },
            characterClass = characterClass,
            race = race,
            level = 1,
            alignment = "Neutro",
            deity = "Nenhuma",
            size = raceInfo.size,
            baseSpeedMeters = raceInfo.baseSpeedMeters,
            currentHp = maxOf(1, baseHp),
            maxHp = maxOf(1, baseHp),
            baseStr = 14,
            baseDex = 12,
            baseCon = 14,
            baseInt = 10,
            baseWis = 10,
            baseCha = 10,
            skillsJson = CharacterJsonAdapters.serializeSkills(initialSkills),
            inventoryJson = "[]",
            featsJson = "[]",
            spellsJson = "[]",
            spellSlotsJson = CharacterJsonAdapters.serializeSpellSlots(initialSlots),
            multiclassJson = CharacterJsonAdapters.serializeClassEntries(listOf(com.example.dnd35.model.CharacterClassEntry(characterClass, 1))),
            notes = "",
            updatedAt = System.currentTimeMillis()
        )
        return characterDao.insertCharacter(newChar)
    }

    suspend fun getAllCharactersOnce(): List<CharacterEntity> = characterDao.getAllCharactersOnce()

    /** Insere fichas importadas como registros novos (id = 0). Devolve os novos ids, na mesma ordem. */
    suspend fun importCharacters(characters: List<CharacterEntity>): List<Long> =
        characterDao.insertCharacters(characters.map { it.copy(id = 0) })

    suspend fun duplicateCharacter(character: CharacterEntity): Long {
        val clone = character.copy(
            id = 0,
            name = "${character.name} (Cópia)",
            updatedAt = System.currentTimeMillis()
        )
        return characterDao.insertCharacter(clone)
    }

    suspend fun deleteCharacter(id: Long) {
        characterDao.deleteById(id)
    }
}
