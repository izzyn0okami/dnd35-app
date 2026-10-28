package com.example.dnd35.model

import java.text.Normalizer

enum class FeatPrerequisiteStatus {
    MET,
    NOT_MET,
    PARTIAL
}

data class FeatPrerequisiteAssessment(
    val status: FeatPrerequisiteStatus,
    val unmet: List<String> = emptyList(),
    val unverified: List<String> = emptyList()
) {
    val message: String
        get() = when (status) {
            FeatPrerequisiteStatus.MET -> "Pré-requisitos cumpridos"
            FeatPrerequisiteStatus.NOT_MET ->
                "Pré-requisitos não cumpridos: ${unmet.joinToString("; ")}"
            FeatPrerequisiteStatus.PARTIAL -> buildList {
                if (unmet.isNotEmpty()) add("Pendentes: ${unmet.joinToString("; ")}")
                if (unverified.isNotEmpty()) add("Não verificados: ${unverified.joinToString("; ")}")
            }.joinToString(" · ").ifBlank { "Verificação parcial" }
        }
}

/**
 * Verificação informativa dos pré-requisitos cadastrados no catálogo.
 * Nunca impede selecionar, editar ou manter um talento na ficha.
 *
 * Pré-requisitos que dependem de escolhas não registradas pela ficha (por exemplo,
 * proficiência com uma arma específica ou escola escolhida para Foco em Magia) ficam
 * explicitamente como não verificados, em vez de serem aprovados ou reprovados por suposição.
 */
object FeatPrerequisiteChecker {
    private val abilityRequirement = Regex("\\b(for|str|des|dex|con|int|sab|wis|car|cha)\\s*(\\d+)\\b")
    private val babRequirement = Regex("\\bbba\\s*\\+?\\s*(\\d+)\\b")
    private val skillRequirement = Regex("^(.+?)\\s+(\\d+)\\s+graduacoes?$")
    private val characterLevelRequirement = Regex("^(\\d+)[º°o]?\\s+nivel de personagem$")
    private val casterLevelRequirement = Regex("^conjurador de (\\d+)[º°o]?\\s+nivel$")
    private val casterLevelRequirementReversed = Regex("^(\\d+)[º°o]?\\s+nivel de conjurador$")
    private val classLevelRequirement = Regex("^(\\d+)[º°o]?\\s+nivel de (.+)$")
    private val classLevelRequirementReversed = Regex("^(.+?)\\s+(\\d+)[º°o]?\\s+nivel$")

    private val metamagicFeats = setOf(
        "acelerar magia", "aumentar magia", "estender magia", "maximizar magia",
        "potencializar magia", "prolongar magia", "silenciar magia", "magia sem gestos",
        "magia sem palavras", "substituição de energia"
    )

    fun assess(prerequisites: String, stats: CalculatedCharacterStats): FeatPrerequisiteAssessment {
        val raw = prerequisites.trim()
        if (raw.isBlank() || normalize(raw) in setOf("nenhum", "nenhuma", "nao")) {
            return FeatPrerequisiteAssessment(FeatPrerequisiteStatus.MET)
        }

        val unmet = mutableListOf<String>()
        val unverified = mutableListOf<String>()
        val clauses = raw.split(",").map { it.trim() }.filter { it.isNotBlank() }

        clauses.forEach { clause ->
            val normalized = normalize(clause)
            when {
                normalized in setOf("nenhum", "nenhuma") -> Unit
                abilityRequirement.containsMatchIn(normalized) -> {
                    val match = abilityRequirement.find(normalized)!!
                    val ability = match.groupValues[1]
                    val required = match.groupValues[2].toInt()
                    val actual = when (ability) {
                        "for", "str" -> stats.totalStr
                        "des", "dex" -> stats.totalDex
                        "con" -> stats.totalCon
                        "int" -> stats.totalInt
                        "sab", "wis" -> stats.totalWis
                        else -> stats.totalCha
                    }
                    if (actual < required) unmet += "$clause (atual: $actual)"
                }
                babRequirement.containsMatchIn(normalized) -> {
                    val required = babRequirement.find(normalized)!!.groupValues[1].toInt()
                    if (stats.bab < required) unmet += "$clause (atual: +${stats.bab})"
                }
                skillRequirement.matches(normalized) -> {
                    val match = skillRequirement.matchEntire(normalized)!!
                    val name = normalize(match.groupValues[1])
                    val required = match.groupValues[2].toFloat()
                    val skill = stats.skills.firstOrNull { normalize(it.name) == name }
                    if (skill == null || skill.ranks < required) {
                        unmet += "$clause (graduações registradas: ${skill?.ranks?.toInt() ?: 0})"
                    }
                }
                characterLevelRequirement.matches(normalized) -> {
                    val required = characterLevelRequirement.matchEntire(normalized)!!.groupValues[1].toInt()
                    if (stats.totalCharacterLevel < required) {
                        unmet += "$clause (nível atual: ${stats.totalCharacterLevel})"
                    }
                }
                casterLevelRequirement.matches(normalized) || casterLevelRequirementReversed.matches(normalized) -> {
                    val required = (casterLevelRequirement.matchEntire(normalized)
                        ?: casterLevelRequirementReversed.matchEntire(normalized))!!.groupValues[1].toInt()
                    val actual = stats.casterClasses.maxOfOrNull { it.casterLevel } ?: 0
                    if (actual < required) unmet += "$clause (maior nível de conjurador registrado: $actual)"
                }
                normalized.startsWith("capacidade de conjurar magias de ") -> {
                    val match = Regex("magias de (\\d+)[º°o]? nivel").find(normalized)
                    val requiredSpellLevel = match?.groupValues?.get(1)?.toIntOrNull()
                    if (requiredSpellLevel == null) {
                        unverified += clause
                    } else if (stats.casterClasses.none { caster ->
                            (caster.dcByLevel.keys.maxOrNull() ?: 0) >= requiredSpellLevel
                        }) {
                        unmet += clause
                    }
                }
                classLevelRequirement.matches(normalized) || classLevelRequirementReversed.matches(normalized) -> {
                    val match = classLevelRequirement.matchEntire(normalized)
                    val reversed = classLevelRequirementReversed.matchEntire(normalized)
                    val requiredClass = normalize(match?.groupValues?.get(2) ?: reversed!!.groupValues[1])
                    val requiredLevel = (match?.groupValues?.get(1) ?: reversed!!.groupValues[2]).toInt()
                    val actual = stats.classEntries.filter { normalize(it.className) == requiredClass }.sumOf { it.level }
                    if (actual < requiredLevel) unmet += "$clause (nível registrado: $actual)"
                }
                normalized.contains("habilidade de forma selvagem") -> {
                    val available = stats.activeClassFeatures.any {
                        normalize(it.name).contains("forma selvagem")
                    }
                    if (!available) unmet += clause
                }
                normalized.contains("habilidade de expulsar") || normalized.contains("fascinar criaturas") -> {
                    val available = stats.activeClassFeatures.any {
                        val name = normalize(it.name)
                        name.contains("expulsar") || name.contains("fascinar")
                    }
                    if (!available) unmet += clause
                }
                normalized == "metamágico" -> {
                    if (stats.feats.none { normalize(it.name) in metamagicFeats }) unmet += clause
                }
                else -> evaluateFeatOrMarkUnknown(clause, normalized, stats, unmet, unverified)
            }
        }

        val status = when {
            unmet.isEmpty() && unverified.isEmpty() -> FeatPrerequisiteStatus.MET
            unmet.isNotEmpty() && unverified.isEmpty() -> FeatPrerequisiteStatus.NOT_MET
            else -> FeatPrerequisiteStatus.PARTIAL
        }
        return FeatPrerequisiteAssessment(status, unmet.distinct(), unverified.distinct())
    }

    private fun evaluateFeatOrMarkUnknown(
        clause: String,
        normalized: String,
        stats: CalculatedCharacterStats,
        unmet: MutableList<String>,
        unverified: MutableList<String>
    ) {
        val catalogNames = Dnd35Rules.PRESET_FEATS.map { normalize(it.name) }
        val matchingFeat = catalogNames
            .filter { name -> normalized == name || normalized.startsWith("$name ") }
            .maxByOrNull { it.length }

        if (matchingFeat != null) {
            // Talentos que exigem uma escolha podem ser registrados com essa escolha
            // no nome (por exemplo, "Foco em Arma (Espada Longa)"). Comparar apenas
            // igualdade exata gerava falso negativo para talentos já presentes na ficha.
            val hasFeat = stats.feats.any { characterFeat ->
                val registeredName = normalize(characterFeat.name)
                registeredName == matchingFeat ||
                    registeredName.startsWith("$matchingFeat (") ||
                    registeredName.startsWith("$matchingFeat:") ||
                    registeredName.startsWith("$matchingFeat -")
            }
            if (!hasFeat) unmet += clause
            if (normalized.contains("mesma escola") || normalized.contains("arma escolhida") || normalized.contains("foco em magia (")) {
                unverified += "$clause (a escola/arma escolhida não é registrada)"
            }
        } else {
            // A ficha não registra todas as proficiências, escolhas de armas/escolas ou
            // habilidades de classe opcionais; não afirmar que esses requisitos são satisfeitos.
            unverified += clause
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .replace("º", "o")
            .replace("°", "o")
            .replace(Regex("\\s+"), " ")
            .trim()
}
