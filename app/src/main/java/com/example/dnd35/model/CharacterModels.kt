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
    // Não inferir gastos antigos a partir das graduações atuais.
    val skillPointHistoryJson: String = "",
    // Vazio em fichas antigas: não presumir que idiomas já cadastrados foram comprados.
    val languageAcquisitionsJson: String = "",
    val inventoryJson: String = "",
    val featsJson: String = "",
    val spellsJson: String = "",
    val spellSlotsJson: String = "",
    val multiclassJson: String = "",
    val notes: String = "",
    // Marcação usada só na Sessão de Mesa: fichas de NPC não representam um jogador
    // conectado, são controladas inteiramente pelo Mestre (loja, monstros, etc.).
    val isNpc: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class SkillPointAllocation(val skillName: String, val pointsSpent: Float)
data class SkillPointLevelRecord(
    val characterLevel: Int,
    val className: String,
    val availablePoints: Float,
    val allocations: List<SkillPointAllocation> = emptyList()
)

enum class LanguageAcquisitionSource { RACIAL, INTELLIGENCE, CLASS_FEATURE, FEAT, PURCHASED, OTHER }

data class LanguageAcquisition(
    val language: String,
    val source: LanguageAcquisitionSource,
    val characterLevel: Int? = null,
    val pointsSpent: Int = 0
) {
    fun isValid(): Boolean = language.isNotBlank() &&
        when (source) {
            LanguageAcquisitionSource.PURCHASED -> characterLevel != null && characterLevel >= 1 && pointsSpent == 1
            else -> pointsSpent == 0 && (characterLevel == null || characterLevel >= 1)
        }
}

data class CharacterClassEntry(val className: String, val level: Int = 1)
data class CharacterSkill(
    val name: String,
    val keyAbility: String,
    val ranks: Float = 0.0f,
    val miscBonus: Int = 0,
    val isClassSkill: Boolean = false,
    val notes: String = ""
)
data class CharacterInventoryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val quantity: Int = 1,
    val weightLb: Float = 1.0f,
    val costGp: Float = 0.0f,
    val damageOrAc: String = "",
    val equipped: Boolean = false,
    val equippedSlot: String = Dnd35Rules.EquipmentSlots.NONE,
    val notes: String = "",
    val inBackpack: Boolean = true,
    /** URI persistível da imagem personalizada do item; nulo para itens sem foto. */
    val imageUri: String? = null,
    /** Munição restante dentro dos pacotes; nulo para registros antigos ainda não consumidos. */
    val ammunitionUnitsRemaining: Int? = null,
    /** Quantidade de projéteis por pacote; 1 representa item avulso. */
    val ammunitionUnitsPerPackage: Int? = null
)

/** Quantidade de unidades individuais que uma embalagem representa (ex.: Flechas (20)). */
fun ammunitionBundleSize(item: CharacterInventoryItem): Int {
    item.ammunitionUnitsPerPackage?.takeIf { it > 0 }?.let { return it }
    val normalizedName = item.name.lowercase()
    val isKnownProjectile = normalizedName.contains("flecha") ||
        normalizedName.contains("virote") ||
        (normalizedName.contains("bala") && normalizedName.contains("funda"))
    if (!isKnownProjectile) return 1

    return item.name.substringAfterLast('(').substringBeforeLast(')')
        .toIntOrNull()
        ?.takeIf { item.name.endsWith(")") && it > 0 }
        ?: 1
}

/** Quantidade de projéteis disponíveis, distinguindo pacotes do número de disparos. */
fun remainingAmmunitionUnits(item: CharacterInventoryItem): Int =
    (item.ammunitionUnitsRemaining ?: (item.quantity * ammunitionBundleSize(item))).coerceAtLeast(0)

/** Consome uma unidade da pilha explicitamente selecionada no slot de munição. */
fun consumeAmmunitionStack(items: List<CharacterInventoryItem>, ammunitionItemId: String): List<CharacterInventoryItem>? {
    val ammunition = items.firstOrNull {
        it.id == ammunitionItemId &&
            it.equippedSlot == Dnd35Rules.EquipmentSlots.AMMUNITION &&
            it.quantity > 0 &&
            remainingAmmunitionUnits(it) > 0
    } ?: return null

    val remaining = remainingAmmunitionUnits(ammunition) - 1
    if (remaining <= 0) return items.filterNot { it.id == ammunition.id }

    val bundleSize = ammunitionBundleSize(ammunition)
    val packagesRemaining = (remaining + bundleSize - 1) / bundleSize
    return items.map {
        if (it.id == ammunition.id) {
            it.copy(quantity = packagesRemaining, ammunitionUnitsRemaining = remaining, ammunitionUnitsPerPackage = bundleSize)
        } else {
            it
        }
    }
}
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
data class SpellSlotInfo(val level: Int, val totalSlots: Int = 0, val usedSlots: Int = 0)
