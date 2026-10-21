package com.example.dnd35.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "Novo Aventureiro",
    val characterClass: String = "Guerreiro",
    val race: String = "Humano",
    val level: Int = 1,
    val alignment: String = "Neutro e Bom",
    val deity: String = "Pelor",
    val size: String = "Médio",
    val photoUri: String? = null,
    val currentHp: Int = 12,
    val maxHp: Int = 12,
    val tempHp: Int = 0,
    val nonLethalDamage: Int = 0,
    val baseStr: Int = 14,
    val baseDex: Int = 12,
    val baseCon: Int = 14,
    val baseInt: Int = 10,
    val baseWis: Int = 10,
    val baseCha: Int = 10,
    val modStrBonus: Int = 0,
    val modDexBonus: Int = 0,
    val modConBonus: Int = 0,
    val modIntBonus: Int = 0,
    val modWisBonus: Int = 0,
    val modChaBonus: Int = 0,
    val armorBonus: Int = 0,
    val shieldBonus: Int = 0,
    val naturalArmor: Int = 0,
    val deflectionBonus: Int = 0,
    val miscAcBonus: Int = 0,
    val miscFortitude: Int = 0,
    val miscReflex: Int = 0,
    val miscWill: Int = 0,
    val miscInitiative: Int = 0,
    val baseSpeedMeters: Float = 9.0f,
    val goldGp: Float = 50.0f,
    val silverSp: Float = 0.0f,
    val copperCp: Float = 0.0f,
    val platinumPp: Float = 0.0f,
    val skillsJson: String = "",
    val languagesJson: String = "",
    // Histórico opcional; não inferir gastos antigos a partir das graduações atuais.
    val skillPointHistoryJson: String = "",
    val inventoryJson: String = "",
    val featsJson: String = "",
    val spellsJson: String = "",
    val spellSlotsJson: String = "",
    val multiclassJson: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class SkillPointAllocation(val skillName: String, val pointsSpent: Float)

data class SkillPointLevelRecord(
    val characterLevel: Int,
    val className: String,
    val availablePoints: Float,
    val allocations: List<SkillPointAllocation> = emptyList()
)

data class CharacterClassEntry(
    val className: String,
    val level: Int = 1
)

data class CharacterSkill(
    val name: String,
    val keyAbility: String, // FOR, DES, CON, INT, SAB, CAR
    val ranks: Float = 0.0f,
    val miscBonus: Int = 0,
    val isClassSkill: Boolean = false,
    val notes: String = ""
)

data class CharacterInventoryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // Armas, Armaduras, Escudos, Itens de Aventura, Itens Mágicos, Consumíveis, Outros
    val quantity: Int = 1,
    val weightLb: Float = 1.0f,
    val costGp: Float = 0.0f,
    val damageOrAc: String = "",
    val equipped: Boolean = false,
    val equippedSlot: String = Dnd35Rules.EquipmentSlots.NONE, // Espaço corporal atribuído
    val notes: String = ""
)

data class CharacterFeat(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val prerequisites: String = "",
    val summary: String = "",
    val description: String = ""
)

data class CharacterSpell(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val level: Int = 0,
    val school: String = "",
    val castTime: String = "1 ação padrão",
    val components: String = "V, S",
    val range: String = "Toque",
    val duration: String = "Instantânea",
    val savingThrow: String = "Nenhum",
    val spellResistance: Boolean = true,
    val prepared: Boolean = true,
    val preparedCount: Int = 1,
    val description: String = ""
)

data class SpellSlotInfo(
    val level: Int,
    val totalSlots: Int = 0,
    val usedSlots: Int = 0
)
