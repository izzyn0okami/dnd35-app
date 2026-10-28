package com.example.dnd35.model

import java.text.Normalizer

/**
 * Interpreta a linha de escola do Livro do Jogador sem alterar o texto legado
 * armazenado no catálogo/ficha. Subescolas e textos entre colchetes são preservados
 * separadamente para que filtros por escola básica não dependam da redação exata.
 */
data class MagicSchoolInfo(
    val baseSchool: String,
    val subschool: String? = null,
    val descriptors: List<String> = emptyList(),
    val annotations: List<String> = emptyList()
)

object MagicSchoolParser {
    private val parenthetical = Regex("\\(([^()]*)\\)")
    private val bracketed = Regex("\\[([^\\]]*)\\]")
    private val knownDescriptors = setOf(
        "acido", "agua", "ar", "audivel", "bem", "caos", "eletricidade",
        "energia", "fogo", "forca", "frio", "luz", "mal", "medo",
        "morte", "planta", "sônico", "sonico", "terra", "acao mental",
        "mental", "teletransporte"
    )

    fun parse(value: String): MagicSchoolInfo {
        val text = value.trim()
        val base = Regex("^([^\\s\\[(]+)").find(text)?.groupValues?.get(1).orEmpty()
        val subschool = parenthetical.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
        val bracketContents = bracketed.findAll(text).map { it.groupValues[1].trim() }.filter { it.isNotBlank() }.toList()
        val descriptors = bracketContents.filter { normalize(it).removePrefix("de ") in knownDescriptors }
        val annotations = bracketContents.filterNot { it in descriptors }
        return MagicSchoolInfo(
            baseSchool = base,
            subschool = subschool,
            descriptors = descriptors,
            annotations = annotations
        )
    }

    fun matchesBaseSchool(school: String, selectedSchool: String): Boolean {
        if (selectedSchool == "Todas") return true
        return parse(school).baseSchool.equals(selectedSchool, ignoreCase = true)
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
}
