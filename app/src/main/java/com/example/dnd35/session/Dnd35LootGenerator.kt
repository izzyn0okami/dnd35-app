package com.example.dnd35.session

import com.example.dnd35.model.Dnd35Rules
import java.text.Normalizer
import kotlin.random.Random

/**
 * Tesouro aleatório do Livro do Mestre 3.5: três rolagens d% independentes
 * (moedas, bens e itens), usando o ND do encontro.
 */
object Dnd35LootGenerator {
    data class GeneratedItem(
        val name: String,
        val category: String,
        val quantity: Int = 1,
        val valueGp: Float,
        val isCurrency: Boolean = false,
        val notes: String = ""
    )
    data class RandomTreasure(
        val encounterLevel: Int,
        val coinsRoll: Int,
        val goodsRoll: Int,
        val itemsRoll: Int,
        val items: List<GeneratedItem>,
        val summary: String
    )
    private data class Entry(val range: IntRange, val kind: String, val dice: String = "")
    private data class Dice(val count: Int, val sides: Int, val multiplier: Int = 1, val unit: String = "")

    private val coinRows = listOf(
        "1-14:none;15-29:coins:1d6x1000cp;30-52:coins:1d8x100sp;53-95:coins:2d8x10gp;96-100:coins:1d4x10pp",
        "1-13:none;14-23:coins:1d10x1000cp;24-43:coins:2d10x100sp;44-95:coins:4d10x10gp;96-100:coins:2d8x10pp",
        "1-11:none;12-21:coins:2d10x1000cp;22-41:coins:4d8x100sp;42-95:coins:1d4x100gp;96-100:coins:1d10x10pp",
        "1-11:none;12-21:coins:3d10x1000cp;22-41:coins:4d12x1000sp;42-95:coins:1d6x100gp;96-100:coins:1d8x10pp",
        "1-10:none;11-19:coins:1d4x10000cp;20-38:coins:1d6x1000sp;39-95:coins:1d8x100gp;96-100:coins:1d10x10pp",
        "1-10:none;11-18:coins:1d6x10000cp;19-37:coins:1d8x1000sp;38-95:coins:1d10x100gp;96-100:coins:1d12x10pp",
        "1-11:none;12-18:coins:1d10x10000cp;19-35:coins:1d12x1000sp;36-93:coins:2d6x100gp;94-100:coins:3d4x10pp",
        "1-10:none;11-15:coins:1d12x10000cp;16-29:coins:2d6x1000sp;30-87:coins:2d8x100gp;88-100:coins:3d6x10pp",
        "1-10:none;11-15:coins:2d6x10000cp;16-29:coins:2d8x1000sp;30-85:coins:5d4x100gp;86-100:coins:2d12x10pp",
        "1-10:none;11-24:coins:2d10x1000sp;25-79:coins:6d4x100gp;80-100:coins:5d6x10pp",
        "1-8:none;9-14:coins:3d10x1000sp;15-75:coins:4d8x100gp;76-100:coins:4d10x10pp",
        "1-8:none;9-14:coins:3d12x1000sp;15-75:coins:1d4x1000gp;76-100:coins:1d4x100pp",
        "1-8:none;9-75:coins:1d4x1000gp;76-100:coins:1d10x100pp",
        "1-8:none;9-75:coins:1d6x1000gp;76-100:coins:1d12x100pp",
        "1-3:none;4-74:coins:1d8x1000gp;75-100:coins:3d4x100pp",
        "1-3:none;4-74:coins:1d12x1000gp;75-100:coins:3d4x100pp",
        "1-3:none;4-68:coins:3d4x1000gp;69-100:coins:2d10x100pp",
        "1-2:none;3-65:coins:3d6x1000gp;66-100:coins:5d4x100pp",
        "1-2:none;3-65:coins:3d8x1000gp;66-100:coins:3d10x100pp",
        "1-2:none;3-65:coins:4d8x1000gp;66-100:coins:4d10x100pp"
    )
    private val goodsRows = listOf(
        "1-90:none;91-95:gem:1;96-100:art:1",
        "1-81:none;82-95:gem:1d3;96-100:art:1d3",
        "1-77:none;78-95:gem:1d3;96-100:art:1d3",
        "1-70:none;71-95:gem:1d4;96-100:art:1d3",
        "1-60:none;61-95:gem:1d4;96-100:art:1d4",
        "1-56:none;57-92:gem:1d4;93-100:art:1d4",
        "1-48:none;49-88:gem:1d4;89-100:art:1d4",
        "1-45:none;46-85:gem:1d6;86-100:art:1d4",
        "1-40:none;41-80:gem:1d8;81-100:art:1d4",
        "1-35:none;36-79:gem:1d8;80-100:art:1d6",
        "1-24:none;25-74:gem:1d10;75-100:art:1d6",
        "1-17:none;18-70:gem:1d10;71-100:art:1d8",
        "1-11:none;12-66:gem:1d12;67-100:art:1d10",
        "1-11:none;12-66:gem:2d8;67-100:art:2d6",
        "1-9:none;10-65:gem:2d10;66-100:art:2d8",
        "1-7:none;8-64:gem:4d6;65-100:art:2d10",
        "1-4:none;5-63:gem:4d8;64-100:art:3d8",
        "1-4:none;5-54:gem:3d12;55-100:art:3d10",
        "1-3:none;4-50:gem:6d6;51-100:art:6d6",
        "1-2:none;3-38:gem:4d10;39-100:art:7d6"
    )
    private val itemRows = listOf(
        "1-71:none;72-95:mundane:1;96-100:minor:1",
        "1-49:none;50-85:mundane:1;86-100:minor:1",
        "1-49:none;50-79:mundane:1d3;80-100:minor:1",
        "1-42:none;43-62:mundane:1d4;63-100:minor:1",
        "1-57:none;58-67:mundane:1d4;68-100:minor:1d3",
        "1-54:none;55-59:mundane:1d4;60-99:minor:1d3;100-100:medium:1",
        "1-51:none;52-97:minor:1d3;98-100:medium:1",
        "1-48:none;49-96:minor:1d4;97-100:medium:1",
        "1-43:none;44-91:minor:1d4;92-100:medium:1",
        "1-40:none;41-88:minor:1d4;89-99:medium:1;100-100:major:1",
        "1-31:none;32-84:minor:1d4;85-98:medium:1;99-100:major:1",
        "1-27:none;28-82:minor:1d6;83-97:medium:1;98-100:major:1",
        "1-19:none;20-73:minor:1d6;74-95:medium:1;96-100:major:1",
        "1-19:none;20-58:minor:1d6;59-92:medium:1;93-100:major:1",
        "1-11:none;12-46:minor:1d10;47-90:medium:1;91-100:major:1",
        "1-40:none;41-46:minor:1d10;47-90:medium:1d3;91-100:major:1",
        "1-33:none;34-83:medium:1d3;84-100:major:1",
        "1-24:none;25-80:medium:1d4;81-100:major:1",
        "1-4:none;5-70:medium:1d4;71-100:major:1",
        "1-25:none;26-65:medium:1d4;66-100:major:1d3"
    )

    private fun parse(spec: String): List<Entry> = spec.split(';').map { token ->
        val parts = token.split(':')
        val bounds = parts[0].split('-').map(String::toInt)
        Entry(bounds[0]..bounds[1], parts[1], parts.getOrElse(2) { "" })
    }
    private fun rollDice(expression: String, random: Random): Int {
        val match = Regex("(\\d+)d(\\d+)(?:x(\\d+))?([a-z]+)?").matchEntire(expression)
            ?: return expression.toIntOrNull() ?: 1
        val count = match.groupValues[1].toInt()
        val sides = match.groupValues[2].toInt()
        val multiplier = match.groupValues[3].ifBlank { "1" }.toInt()
        return (1..count).sumOf { random.nextInt(1, sides + 1) } * multiplier
    }
    private fun select(entries: List<Entry>, roll: Int): Entry = entries.first { roll in it.range }

    /** Tabela 3-8: 01–10 gera armadura/escudo Pequeno; 11–100, Médio. */
    fun armorSizeForRoll(roll: Int): String? = when (roll) {
        in 1..10 -> "Pequena"
        in 11..100 -> "Média"
        else -> null
    }

    private fun normalizeItemName(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .trim()
            .lowercase()

    /**
     * Peso catalogado em lb para itens gerados. Resultados genéricos sem modelo definido
     * retornam null: não se deve inventar o peso de um objeto que o Mestre ainda precisa escolher.
     * Armaduras e escudos Pequenos pesam metade do modelo Médio.
     */
    fun weightForInventory(item: GeneratedItem): Float? {
        if (item.isCurrency) return 0f
        val small = item.name.endsWith("(Pequena)", ignoreCase = true)
        val baseName = item.name.replace(Regex("\\s+\\((?:Pequena|Média)\\)$", RegexOption.IGNORE_CASE), "").trim()
        val normalized = normalizeItemName(baseName)
        val aliases = mapOf(
            "acido" to "acido (frasco)",
            "mochila vazia" to "mochila",
            "meia armadura" to "armadura de meia placa",
            "armadura de batalha" to "armadura completa"
        )
        val lookup = aliases[normalized] ?: normalized
        val preset = Dnd35Rules.PRESET_ITEMS.firstOrNull { normalizeItemName(it.name) == lookup }
            ?: Dnd35Rules.PRESET_ITEMS.firstOrNull {
                val candidate = normalizeItemName(it.name)
                candidate.startsWith("$lookup ") || lookup.startsWith("$candidate ")
            }
            ?: return null
        return if (small && preset.category in setOf("Armaduras", "Escudos")) {
            preset.weightLb / 2f
        } else preset.weightLb
    }

    fun averageTreasureForEncounterLevel(level: Int): Int? {
        if (level !in 1..20) return null
        return intArrayOf(0,300,600,900,1_200,1_600,2_000,2_600,3_400,4_500,5_800,7_500,9_800,13_000,17_000,22_000,28_000,36_000,47_000,61_000,80_000)[level]
    }

    fun generateRandomTreasure(level: Int, random: Random = Random.Default): RandomTreasure? {
        if (level !in 1..20) return null
        val coinRoll = random.nextInt(1, 101)
        val goodsRoll = random.nextInt(1, 101)
        val itemRoll = random.nextInt(1, 101)
        val result = mutableListOf<GeneratedItem>()

        val coin = select(parse(coinRows[level - 1]), coinRoll)
        if (coin.kind != "none") {
            val amount = rollDice(coin.dice, random)
            val denomination = coin.dice.takeLast(2).uppercase()
            val gp = when (denomination) {
                "CP" -> amount / 100f
                "SP" -> amount / 10f
                "GP" -> amount.toFloat()
                "PP" -> amount * 10f
                else -> 0f
            }
            result += GeneratedItem("Moedas: " + amount + " " + denomination, "Moedas",
                valueGp = gp, isCurrency = true,
                notes = "Tabela 3-5; rolagem de moedas d% " + coinRoll + ".")
        }

        val goods = select(parse(goodsRows[level - 1]), goodsRoll)
        if (goods.kind == "gem" || goods.kind == "art") {
            val count = rollDice(goods.dice, random)
            repeat(count) {
                if (goods.kind == "gem") {
                    val d = random.nextInt(1, 101)
                    val band = when (d) {
                        in 1..25 -> "4d4"
                        in 26..50 -> "2d4x10"
                        in 51..70 -> "4d4x10"
                        in 71..90 -> "2d4x100"
                        in 91..99 -> "4d4x100"
                        else -> "2d4x1000"
                    }
                    val value = rollDice(band, random)
                    result += GeneratedItem("Gema (" + value + " PO)", "Itens de Aventura",
                        valueGp = value.toFloat(), notes = "Gema; rolagem d% " + d + " na tabela de gemas.")
                } else {
                    val d = random.nextInt(1, 101)
                    val band = when (d) {
                        in 1..10 -> "1d10x10"
                        in 11..25 -> "3d6x10"
                        in 26..40 -> "1d6x100"
                        in 41..50 -> "1d10x100"
                        in 51..60 -> "2d6x100"
                        in 61..70 -> "3d6x100"
                        in 71..80 -> "4d6x100"
                        in 81..85 -> "5d6x100"
                        in 86..90 -> "1d4x1000"
                        in 91..95 -> "1d6x1000"
                        in 96..99 -> "2d4x1000"
                        else -> "2d6x1000"
                    }
                    val value = rollDice(band, random)
                    result += GeneratedItem("Objeto de arte (" + value + " PO)", "Itens de Aventura",
                        valueGp = value.toFloat(), notes = "Objeto de arte; rolagem d% " + d + " na tabela de objetos de arte.")
                }
            }
        }

        val item = select(parse(itemRows[level - 1]), itemRoll)
        if (item.kind == "mundane") {
            repeat(rollDice(item.dice, random)) { result += generateMundane(random) }
        } else if (item.kind == "minor" || item.kind == "medium" || item.kind == "major") {
            repeat(rollDice(item.dice, random)) { result += generateMagic(item.kind, random) }
        }
        val summary = "ND " + level + " — d% moedas: " + coinRoll + "; bens: " + goodsRoll + "; itens: " + itemRoll + "."
        return RandomTreasure(level, coinRoll, goodsRoll, itemRoll, result, summary)
    }

    private fun generateMundane(random: Random): GeneratedItem {
        val categoryRoll = random.nextInt(1, 101)
        if (categoryRoll <= 17) {
            val d = random.nextInt(1, 101)
            val entry = when (d) {
                in 1..12 -> Triple("Fogo alquímico", 20f, "1d4")
                in 13..24 -> Triple("Ácido", 10f, "2d4")
                in 25..36 -> Triple("Bastão de fumaça", 20f, "1d4")
                in 37..48 -> Triple("Água benta", 25f, "1d4")
                in 49..62 -> Triple("Antitoxina", 50f, "1d4")
                in 63..74 -> Triple("Tocha eterna", 110f, "1")
                in 75..88 -> Triple("Bolsa de cola", 50f, "1d4")
                else -> Triple("Pedra trovejante", 30f, "1d4")
            }
            val quantity = rollDice(entry.third, random)
            return GeneratedItem(entry.first, "Consumíveis", quantity, entry.second * quantity,
                notes = "Item mundano alquímico; rolagem complementar d% " + d + " na tabela do Livro do Mestre.")
        }
        if (categoryRoll <= 50) {
            val d = random.nextInt(1, 101)
            val armor = when (d) {
                in 1..12 -> Triple("Camisão de cota de malha", 100f, "Armaduras")
                in 13..18 -> Triple("Couro batido obra-prima", 175f, "Armaduras")
                in 19..26 -> Triple("Peitoral de aço", 200f, "Armaduras")
                in 27..34 -> Triple("Cota de bandas", 250f, "Armaduras")
                in 35..54 -> Triple("Meia armadura", 600f, "Armaduras")
                in 55..80 -> Triple("Armadura de batalha", 1500f, "Armaduras")
                in 81..90 -> {
                    val shieldRoll = random.nextInt(1, 101)
                    if (shieldRoll <= 50) Triple("Broquel de madeira negra", 205f, "Escudos")
                    else Triple("Escudo de madeira negra", 257f, "Escudos")
                }
                else -> {
                    val shieldRoll = random.nextInt(1, 101)
                    when (shieldRoll) {
                        in 1..17 -> Triple("Broquel obra-prima", 165f, "Escudos")
                        in 18..40 -> Triple("Escudo pequeno de madeira obra-prima", 153f, "Escudos")
                        in 41..60 -> Triple("Escudo pequeno de metal obra-prima", 159f, "Escudos")
                        in 61..83 -> Triple("Escudo grande de madeira obra-prima", 157f, "Escudos")
                        else -> Triple("Escudo grande de metal obra-prima", 170f, "Escudos")
                    }
                }
            }
            val sizeRoll = random.nextInt(1, 101)
            val size = armorSizeForRoll(sizeRoll) ?: error("Rolagem de tamanho fora de 1..100: $sizeRoll")
            return GeneratedItem("${armor.first} ($size)", armor.third, valueGp = armor.second,
                notes = "Item mundano da tabela de armaduras e escudos; rolagem d% de tamanho: $sizeRoll ($size).")
        }
        if (categoryRoll <= 83) {
            val d = random.nextInt(1, 101)
            val name = when (d) {
                in 1..50 -> "Arma corpo a corpo comum obra-prima"
                in 51..70 -> "Arma corpo a corpo incomum obra-prima"
                else -> "Arma de ataque à distância comum obra-prima"
            }
            return GeneratedItem(name, "Armas", valueGp = 350f,
                notes = "A tabela determina a categoria de arma obra-prima; escolha o modelo específico no catálogo.")
        }
        val d = random.nextInt(1, 101)
        val chosen = when (d) {
            in 1..3 -> "Mochila vazia" to 2f
            in 4..6 -> "Pé de cabra" to 2f
            in 7..11 -> "Lanterna furta-fogo" to 12f
            in 12..16 -> "Fechadura simples" to 20f
            in 17..21 -> "Fechadura média" to 40f
            in 22..28 -> "Fechadura boa" to 80f
            in 29..35 -> "Fechadura superior" to 150f
            in 36..40 -> "Algemas obra-prima" to 50f
            in 41..43 -> "Espelho pequeno de aço" to 10f
            in 44..46 -> "Corda de seda" to 10f
            in 47..53 -> "Luneta" to 1000f
            in 54..58 -> "Ferramentas de artesão obra-prima" to 55f
            in 59..63 -> "Kit de escalada" to 80f
            in 64..68 -> "Kit de disfarces" to 50f
            in 69..73 -> "Kit de cura" to 50f
            in 74..77 -> "Símbolo sagrado de prata" to 25f
            in 78..81 -> "Ampulheta" to 25f
            in 82..88 -> "Lupa" to 100f
            in 89..95 -> "Instrumento musical obra-prima" to 100f
            else -> "Ferramentas de ladrão obra-prima" to 50f
        }
        return GeneratedItem(chosen.first, "Itens de Aventura", valueGp = chosen.second,
            notes = "Item mundano da tabela de ferramentas e equipamentos; rolagem complementar d% " + d + ".")
    }

    private fun generateMagic(tier: String, random: Random): GeneratedItem {
        val d = random.nextInt(1, 101)
        val type = when (tier) {
            "minor" -> when (d) {
                in 1..4 -> "Armaduras e escudos"; in 5..9 -> "Armas"; in 10..44 -> "Poções"
                in 45..46 -> "Anéis"; in 47..81 -> "Pergaminhos"; in 82..91 -> "Varinhas"
                else -> "Itens maravilhosos"
            }
            "medium" -> when (d) {
                in 1..10 -> "Armaduras e escudos"; in 11..20 -> "Armas"; in 21..30 -> "Poções"
                in 31..40 -> "Anéis"; in 41..50 -> "Bastões"; in 51..65 -> "Pergaminhos"
                in 66..68 -> "Cajados"; in 69..83 -> "Varinhas"; else -> "Itens maravilhosos"
            }
            else -> when (d) {
                in 1..10 -> "Armaduras e escudos"; in 11..20 -> "Armas"; in 21..25 -> "Poções"
                in 26..35 -> "Anéis"; in 36..45 -> "Bastões"; in 46..55 -> "Pergaminhos"
                in 56..75 -> "Cajados"; in 76..80 -> "Varinhas"; else -> "Itens maravilhosos"
            }
        }
        val minCost = when (tier) { "minor" -> 1f; "medium" -> 5_000f; else -> 25_000f }
        val maxCost = when (tier) { "minor" -> 5_000f; "medium" -> 25_000f; else -> Float.MAX_VALUE }
        val candidates = Dnd35Rules.PRESET_ITEMS.filter { preset ->
            val pricedForTier = preset.costGp >= minCost && preset.costGp < maxCost
            val magical = preset.name.contains("+") ||
                preset.properties.contains("mágic", ignoreCase = true) ||
                preset.properties.contains("encant", ignoreCase = true)
            pricedForTier && when (type) {
                "Armaduras e escudos" -> preset.category in setOf("Armaduras","Escudos") && magical
                "Armas" -> preset.category == "Armas" && magical
                "Poções" -> preset.name.contains("Poção", true)
                "Anéis" -> preset.name.contains("Anel", true)
                "Bastões" -> preset.name.contains("Bastão", true)
                "Pergaminhos" -> preset.name.contains("Pergaminho", true)
                "Cajados" -> preset.name.contains("Cajado", true)
                "Varinhas" -> preset.name.contains("Varinha", true)
                else -> preset.category == "Itens Mágicos" && magical &&
                    !preset.name.contains("Anel", true) && !preset.name.contains("Bastão", true) &&
                    !preset.name.contains("Cajado", true) && !preset.name.contains("Varinha", true) &&
                    !preset.name.contains("Pergaminho", true)
            }
        }
        val selected = candidates.randomOrNull(random)
        val label = when (tier) { "minor" -> "menor"; "medium" -> "médio"; else -> "maior" }
        val average = when (tier) { "minor" -> 1000f; "medium" -> 10000f; else -> 40000f }
        return if (selected != null) {
            GeneratedItem(selected.name, selected.category, valueGp = selected.costGp,
                notes = "Item mágico " + label + "; categoria " + type + " obtida com d% " + d + " na Tabela 7-1. Selecionado do catálogo disponível.")
        } else {
            GeneratedItem("Item mágico " + label + " — " + type, "Itens Mágicos", valueGp = average,
                notes = "Categoria obtida com d% " + d + " na Tabela 7-1; escolha o item específico na tabela correspondente do Livro do Mestre.")
        }
    }
}
