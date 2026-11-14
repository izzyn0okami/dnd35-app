package com.example.dnd35.model

import java.text.Normalizer
import kotlin.random.Random

/**
 * Conjuntos de equipamento de 1º nível do Livro do Jogador 3.5 (edição brasileira).
 * Somente o equipamento é aplicado automaticamente; perícias, talentos e magias continuam
 * sendo escolhas do jogador nas respectivas seções da ficha.
 */
data class StartingEquipmentKit(
    val items: List<CharacterInventoryItem>,
    val goldDiceCount: Int,
    val goldDieSides: Int,
    val goldMultiplier: Int = 1
) {
    fun rollStartingGold(): Float =
        (1..goldDiceCount).sumOf { Random.nextInt(1, goldDieSides + 1) } * goldMultiplier.toFloat()
}

object StartingEquipmentKits {
    private data class ItemSpec(
        val name: String,
        val quantity: Int = 1,
        val category: String = "Itens de Aventura",
        val weightKg: Float? = null,
        val costGpOverride: Float? = null,
        val notes: String? = null
    )

    private fun normalized(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .trim()
            .lowercase()

    private fun item(spec: ItemSpec): CharacterInventoryItem {
        val catalogName = when (normalized(spec.name)) {
            "corselete de couro batido" -> "Couro Batido"
            else -> spec.name
        }
        val preset = Dnd35Rules.PRESET_ITEMS.firstOrNull { normalized(it.name) == normalized(catalogName) }
        return if (preset != null) {
            CharacterInventoryItem(
                name = preset.name,
                category = preset.category,
                quantity = spec.quantity,
                weightLb = spec.weightKg?.let { MetricUnits.kilogramsToPounds(it) } ?: preset.weightLb,
                costGp = spec.costGpOverride ?: preset.costGp,
                damageOrAc = preset.damageOrAc,
                equippedSlot = preset.defaultSlot,
                iconKey = preset.iconKey,
                notes = spec.notes ?: preset.properties
            )
        } else {
            CharacterInventoryItem(
                name = spec.name,
                category = spec.category,
                quantity = spec.quantity,
                weightLb = spec.weightKg?.let { MetricUnits.kilogramsToPounds(it) } ?: 0f,
                costGp = spec.costGpOverride ?: 0f,
                notes = spec.notes ?: "Item do conjunto inicial do Livro do Jogador; preço não consta no catálogo."
            )
        }
    }

    private fun commonPack(includeBedroll: Boolean = true): List<ItemSpec> = buildList {
        add(ItemSpec("Mochila"))
        add(ItemSpec("Cantil"))
        add(ItemSpec("Rações de Viagem (por dia)"))
        if (includeBedroll) add(ItemSpec("Saco de Dormir"))
        add(ItemSpec("Saco"))
        add(ItemSpec("Pederneira e Isqueiro"))
    }

    private fun kit(
        items: List<ItemSpec>,
        dice: Int,
        sides: Int,
        multiplier: Int = 1
    ) = StartingEquipmentKit(items.map(::item), dice, sides, multiplier)

    fun forClass(characterClass: String, race: String): StartingEquipmentKit? {
        val className = normalized(characterClass)
        val raceName = normalized(race)
        val pack = commonPack()
        val small = raceName.contains("gnomo") || raceName.contains("halfling")

        val baseKit = when {
            className == "barbaro" -> kit(
                listOf(
                    ItemSpec("Couro Batido", category = "Armaduras", weightKg = 10f),
                    ItemSpec("Machado Grande", weightKg = 6f),
                    ItemSpec("Arco Curto", weightKg = 1f),
                    ItemSpec("Adaga"),
                    *pack.toTypedArray(),
                    ItemSpec("Aljava"),
                    ItemSpec("Flechas (20)")
                ), 2, 4
            )
            className == "bardo" -> kit(
                listOf(
                    ItemSpec("Corselete de couro batido", category = "Armaduras", weightKg = 10f),
                    ItemSpec("Espada Longa", weightKg = 2f),
                    ItemSpec("Besta Leve", weightKg = 2f),
                    *pack.toTypedArray(),
                    ItemSpec("Tocha", quantity = 3),
                    ItemSpec("Virotes de Besta (10)"),
                    ItemSpec("Instrumento Musical Comum", notes = "Alaúde comum, conforme o conjunto inicial do Bardo."),
                    ItemSpec("Bolsa de Componentes de Magia")
                ), 2, 4
            )
            className == "clerigo" -> kit(
                listOf(
                    ItemSpec("Brunea"),
                    ItemSpec("Escudo Grande de Madeira", category = "Escudos"),
                    ItemSpec("Maça Pesada", weightKg = 2.4f),
                    ItemSpec("Besta Leve"),
                    *pack.toTypedArray(),
                    ItemSpec("Virotes de Besta (10)"),
                    ItemSpec("Símbolo Sagrado de Madeira"),
                    ItemSpec("Tocha", quantity = 3)
                ), 1, 4
            )
            className == "druida" -> kit(
                listOf(
                    ItemSpec("Gibão de Peles"),
                    ItemSpec("Escudo Grande de Madeira", category = "Escudos"),
                    ItemSpec("Cimitarra"),
                    ItemSpec("Clava"),
                    ItemSpec("Funda"),
                    *pack.toTypedArray(),
                    ItemSpec("Balas de Funda (10)"),
                    ItemSpec("Azevinho e Visco"),
                    ItemSpec("Tocha", quantity = 3)
                ), 1, 6
            )
            className == "feiticeiro" -> kit(
                listOf(
                    ItemSpec("Lança Curta", weightKg = 1.5f),
                    ItemSpec("Besta Leve", weightKg = 2f),
                    *pack.toTypedArray(),
                    ItemSpec("Lanterna Coberta"),
                    ItemSpec("Óleo (500 ml)"),
                    ItemSpec("Bolsa de Componentes de Magia"),
                    ItemSpec("Virotes de Besta (10)")
                ), 3, 4
            )
            className == "guerreiro" && raceName.contains("anao") -> kit(
                listOf(
                    ItemSpec("Brunea"),
                    ItemSpec("Escudo Grande de Madeira", category = "Escudos"),
                    ItemSpec("Machado de Guerra Anão"),
                    ItemSpec("Arco Curto", weightKg = 1f),
                    *pack.toTypedArray(),
                    ItemSpec("Aljava"),
                    ItemSpec("Flechas (20)")
                ), 4, 4
            )
            className == "guerreiro" -> kit(
                listOf(
                    ItemSpec("Brunea"),
                    ItemSpec("Espada Larga")
                ), 0, 4
            )
            className == "ladino" -> kit(
                listOf(
                    ItemSpec("Armadura de Couro", category = "Armaduras", weightKg = 6f),
                    ItemSpec("Espada Curta", weightKg = 1f),
                    ItemSpec("Besta Leve", weightKg = 2f),
                    ItemSpec("Adaga", weightKg = 0.5f),
                    *pack.toTypedArray(),
                    ItemSpec("Instrumentos de Ladrão", category = "Ferramentas"),
                    ItemSpec("Lanterna Coberta"),
                    ItemSpec("Óleo (300 ml)", weightKg = 0.3f, costGpOverride = 0.1f, notes = "Volume do conjunto inicial do Livro do Jogador: 300 ml."),
                    ItemSpec("Virotes de Besta (10)")
                ), 4, 4
            )
            className == "mago" -> kit(
                listOf(
                    ItemSpec("Bordão", weightKg = 2f),
                    ItemSpec("Besta Leve", weightKg = 2f),
                    *pack.toTypedArray(),
                    ItemSpec("Vela", quantity = 10, weightKg = 0.1f),
                    ItemSpec("Porta-mapas", category = "Itens de Aventura", weightKg = 0.2f),
                    ItemSpec("Pergaminho em Branco", quantity = 3),
                    ItemSpec("Tinta (vidro de 30 ml)"),
                    ItemSpec("Caneta Tinteiro"),
                    ItemSpec("Bolsa de Componentes de Magia"),
                    ItemSpec("Grimório de Mago (Em Branco)"),
                    ItemSpec("Virotes de Besta (10)")
                ), 3, 6
            )
            className == "monge" -> kit(
                listOf(
                    ItemSpec("Bordão", weightKg = 2f),
                    ItemSpec("Funda"),
                    *pack.toTypedArray(),
                    ItemSpec("Tocha", quantity = 3),
                    ItemSpec("Balas de Funda (10)")
                ), 2, 4
            )
            className == "paladino" -> kit(
                listOf(
                    ItemSpec("Brunea"),
                    ItemSpec("Escudo Grande de Madeira", category = "Escudos"),
                    ItemSpec("Espada Longa", weightKg = 2f),
                    ItemSpec("Arco Curto", weightKg = 1f),
                    *pack.toTypedArray(),
                    ItemSpec("Lanterna Coberta"),
                    ItemSpec("Óleo (300 ml)", weightKg = 0.3f, costGpOverride = 0.1f, notes = "Volume do conjunto inicial do Livro do Jogador: 300 ml."),
                    ItemSpec("Aljava"),
                    ItemSpec("Flechas (20)"),
                    ItemSpec("Símbolo Sagrado de Madeira")
                ), 6, 4
            )
            // O equipamento do Patrulheiro aparece no próprio bloco da classe no PHB.
            className == "ranger" || className == "patrulheiro" -> kit(
                listOf(
                    ItemSpec("Corselete de couro batido", category = "Armaduras", weightKg = 10f),
                    ItemSpec("Espada Longa", weightKg = 1f),
                    ItemSpec("Espada Curta", weightKg = 1f),
                    ItemSpec("Arco Longo", weightKg = 1.5f)
                ) + commonPack().filterNot { normalized(it.name) == "saco" } + listOf(
                    ItemSpec("Tocha", quantity = 3),
                    ItemSpec("Aljava"),
                    ItemSpec("Flechas (20)")
                ), 2, 4
            )
            else -> null
        }

        // Equipamentos de personagens Pequenos têm metade do peso das versões Médias.
        return if (small) {
            baseKit?.copy(items = baseKit.items.map { item ->
                if (item.category in setOf("Armas", "Armaduras", "Escudos")) {
                    item.copy(weightLb = item.weightLb * 0.5f)
                } else item
            })
        } else baseKit
    }
}
