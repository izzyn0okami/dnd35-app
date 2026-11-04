package com.example.dnd35.model

import java.text.Normalizer

/**
 * Filtros do catálogo de magias do Livro do Jogador. O catálogo guarda tempo de conjuração,
 * alcance, duração e resistência como texto livre ("1 minuto/nível (D)", "Vontade anula
 * (inofensiva)"), então cada filtro agrupa esses textos em poucas categorias.
 */
object SpellFilters {

    const val ALL = "Todos"

    val castTimeOptions = listOf(ALL, "Padrão", "Livre/rápida", "Rodadas", "Minutos ou mais", "Veja texto")
    val rangeOptions = listOf(ALL, "Pessoal", "Toque", "Curto", "Médio", "Longo", "Outro")
    val durationOptions = listOf(ALL, "Instantânea", "Rodadas", "Minutos", "Horas", "Dias ou mais", "Permanente", "Outra")
    val componentOptions = listOf(ALL, "V", "G", "M", "F", "FD", "XP")
    val saveOptions = listOf(ALL, "Nenhum", "Vontade", "Fortitude", "Reflexos")
    val schoolOptions = listOf(
        ALL, "Abjuração", "Adivinhação", "Conjuração", "Encantamento",
        "Evocação", "Ilusão", "Necromancia", "Transmutação", "Universal"
    )
    val classOptions = listOf(ALL, "Mago", "Feiticeiro", "Clérigo", "Druida", "Bardo", "Paladino", "Ranger")

    /** Critérios escolhidos na tela; "Todos"/null significa "sem filtro". */
    data class Criteria(
        val query: String = "",
        val className: String = ALL,
        val level: Int? = null,
        val school: String = ALL,
        val castTime: String = ALL,
        val range: String = ALL,
        val duration: String = ALL,
        val component: String = ALL,
        val save: String = ALL,
        // null = qualquer; true = só com Resistência à Magia; false = só sem.
        val spellResistance: Boolean? = null
    ) {
        /** Quantos filtros além da busca por texto estão ativos (para mostrar um contador). */
        val activeCount: Int
            get() = listOf(
                className != ALL, level != null, school != ALL, castTime != ALL, range != ALL,
                duration != ALL, component != ALL, save != ALL, spellResistance != null
            ).count { it }
    }

    fun matches(spell: Dnd35Rules.PresetSpell, c: Criteria): Boolean {
        if (c.query.isNotBlank()) {
            val q = fold(c.query)
            if (!fold(spell.name).contains(q) && !fold(spell.description).contains(q)) return false
        }
        if (c.className != ALL && SpellbookRules.classLevelOf(spell, c.className) == null) return false
        if (c.level != null) {
            val levels = if (c.className == ALL) {
                spell.classLevels.values + spell.level
            } else {
                listOfNotNull(SpellbookRules.classLevelOf(spell, c.className))
            }
            if (c.level !in levels) return false
        }
        if (c.school != ALL && !MagicSchoolParser.matchesBaseSchool(spell.school, c.school)) return false
        if (c.castTime != ALL && castTimeBucket(spell.castTime) != c.castTime) return false
        if (c.range != ALL && rangeBucket(spell.range) != c.range) return false
        if (c.duration != ALL && durationBucket(spell.duration) != c.duration) return false
        if (c.component != ALL && c.component !in componentTokens(spell.components)) return false
        if (c.save != ALL && !matchesSave(spell.savingThrow, c.save)) return false
        if (c.spellResistance != null && spell.spellResistance != c.spellResistance) return false
        return true
    }

    fun castTimeBucket(text: String): String {
        val t = fold(text)
        return when {
            t.contains("veja texto") && !t.contains("acao padrao") -> "Veja texto"
            t.contains("livre") || t.contains("rapida") || t.contains("imediata") -> "Livre/rápida"
            t.contains("acao padrao") -> "Padrão"
            t.contains("rodada") -> "Rodadas"
            t.contains("minuto") || t.contains("hora") || t.contains("dia") -> "Minutos ou mais"
            else -> "Veja texto"
        }
    }

    fun rangeBucket(text: String): String {
        val t = fold(text)
        return when {
            t.startsWith("pessoal") -> "Pessoal"
            t.startsWith("toque") -> "Toque"
            t.startsWith("curto") || t.startsWith("proximo") -> "Curto"
            t.startsWith("medio") -> "Médio"
            t.startsWith("longo") -> "Longo"
            else -> "Outro"
        }
    }

    fun durationBucket(text: String): String {
        val t = fold(text)
        return when {
            t.startsWith("instantanea") -> "Instantânea"
            t.contains("permanente") -> "Permanente"
            t.contains("rodada") -> "Rodadas"
            t.contains("min") -> "Minutos"
            t.contains("hora") -> "Horas"
            t.contains("dia") || t.contains("semana") || t.contains("mes") || t.contains("ano") -> "Dias ou mais"
            else -> "Outra"
        }
    }

    /** Componentes presentes: V, G, M, F, FD (foco divino) e XP. */
    fun componentTokens(text: String): Set<String> =
        COMPONENT_REGEX.findAll(text).map { it.value }.toSet()

    fun matchesSave(text: String, wanted: String): Boolean {
        val t = fold(text)
        return when (wanted) {
            "Nenhum" -> t.startsWith("nenhum")
            "Vontade" -> t.contains("vontade")
            "Fortitude" -> t.contains("fortitude")
            "Reflexos" -> t.contains("reflexos")
            else -> true
        }
    }

    private val COMPONENT_REGEX = Regex("\\b(?:FD|XP|V|G|M|F)\\b")

    private fun fold(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
}
