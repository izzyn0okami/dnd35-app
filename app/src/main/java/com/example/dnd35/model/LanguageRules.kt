package com.example.dnd35.model

/**
 * Opções de idiomas do Livro do Jogador de D&D 3.5.
 *
 * Este catálogo apenas sugere escolhas: não adiciona nem remove idiomas da ficha.
 * Cenários podem alterar as listas; idiomas de classe, talentos e outras fontes
 * devem ser tratados separadamente.
 */
object LanguageRules {
    data class RaceLanguages(
        val automatic: List<String>,
        /** null indica que a escolha pode ser qualquer idioma permitido pelo cenário. */
        val bonusOptions: List<String>?
    )

    /** Idiomas comuns selecionáveis; campanhas podem acrescentar outros. */
    val coreLanguages = listOf(
        "Abissal", "Anão", "Aquático", "Auran", "Celestial", "Comum",
        "Dracônico", "Élfico", "Gigante", "Gnoll", "Gnomo", "Goblin",
        "Halfling", "Ignan", "Infernal", "Linguagem de Sinais Drow", "Kuo-toa", "Orc", "Silvestre", "Terran",
        "Subterrâneo"
    )

    private val races = mapOf(
        "humano" to RaceLanguages(listOf("Comum"), null),
        "anão" to RaceLanguages(listOf("Anão"), listOf("Gigante", "Gnomo", "Goblin", "Orc", "Terran", "Subterrâneo")),
        "anão da colina" to RaceLanguages(listOf("Anão"), listOf("Gigante", "Gnomo", "Goblin", "Orc", "Terran", "Subterrâneo")),
        "anão da montanha" to RaceLanguages(listOf("Anão"), listOf("Gigante", "Gnomo", "Goblin", "Orc", "Terran", "Subterrâneo")),
        "elfo" to RaceLanguages(listOf("Comum", "Élfico"), listOf("Dracônico", "Gnoll", "Gnomo", "Goblin", "Orc", "Silvestre")),
        "alto elfo" to RaceLanguages(listOf("Comum", "Élfico"), listOf("Dracônico", "Gnoll", "Gnomo", "Goblin", "Orc", "Silvestre")),
        "elfo da floresta" to RaceLanguages(listOf("Comum", "Élfico"), listOf("Dracônico", "Gnoll", "Gnomo", "Goblin", "Orc", "Silvestre")),
        "drow" to RaceLanguages(listOf("Comum", "Élfico", "Subterrâneo"), listOf("Abissal", "Aquático", "Dracônico", "Linguagem de Sinais Drow", "Gnomo", "Goblin", "Kuo-toa")),
        "drow (elfo negro)" to RaceLanguages(listOf("Comum", "Élfico", "Subterrâneo"), listOf("Abissal", "Aquático", "Dracônico", "Linguagem de Sinais Drow", "Gnomo", "Goblin", "Kuo-toa")),
        "gnomo" to RaceLanguages(listOf("Comum", "Gnomo"), listOf("Dracônico", "Élfico", "Gigante", "Goblin", "Anão", "Orc")),
        "gnomo da rocha" to RaceLanguages(listOf("Comum", "Gnomo"), listOf("Dracônico", "Élfico", "Gigante", "Goblin", "Anão", "Orc")),
        "halfling" to RaceLanguages(listOf("Comum", "Halfling"), listOf("Élfico", "Anão", "Gnomo", "Goblin", "Orc")),
        "halfling robusto" to RaceLanguages(listOf("Comum", "Halfling"), listOf("Élfico", "Anão", "Gnomo", "Goblin", "Orc")),
        "meio-elfo" to RaceLanguages(listOf("Comum", "Élfico"), null),
        "meio-orc" to RaceLanguages(listOf("Comum", "Orc"), null)
    )

    fun forRace(raceName: String): RaceLanguages? =
        races[normalize(raceName)]

    /** Quantidade de idiomas adicionais obtidos por Inteligência no 1º nível. */
    fun bonusLanguageCount(intelligenceScore: Int): Int {
        if (intelligenceScore <= 0) return 0
        return ((intelligenceScore - 10) / 2).coerceAtLeast(0)
    }

    fun optionsFor(raceName: String): List<String>? = forRace(raceName)?.bonusOptions

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")
}
