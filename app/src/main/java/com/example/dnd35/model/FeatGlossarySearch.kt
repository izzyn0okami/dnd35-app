package com.example.dnd35.model

import java.text.Normalizer
import java.util.Locale

/**
 * Pesquisa textual no catálogo de talentos do Livro do Jogador.
 * Não altera talentos registrados nem participa da validação mecânica.
 */
data class FeatGlossaryResult(
    val name: String,
    val prerequisites: String,
    val summary: String,
    val benefit: String,
    val source: String = "Livro do Jogador, Capítulo 5: Talentos (p. 73–89)",
    val reference: Dnd35Rules.FeatReference? = null
)

object FeatGlossarySearch {
    fun search(query: String): List<FeatGlossaryResult> {
        val normalized = normalize(query)
        if (normalized.isBlank()) return emptyList()

        val references = Dnd35Rules.FEAT_REFERENCES.associateBy { normalize(it.name) }

        return Dnd35Rules.PRESET_FEATS
            .asSequence()
            .map {
                FeatGlossaryResult(
                    name = it.name,
                    prerequisites = it.prerequisites,
                    summary = it.summary,
                    benefit = it.benefit,
                    reference = references[normalize(it.name)]
                )
            }
            .filter {
                normalize(it.name).contains(normalized) ||
                    normalize(it.prerequisites).contains(normalized) ||
                    normalize(it.summary).contains(normalized) ||
                    normalize(it.benefit).contains(normalized)
            }
            .sortedWith(
                compareBy<FeatGlossaryResult> {
                    if (normalize(it.name) == normalized) 0 else 1
                }.thenBy {
                    if (normalize(it.name).startsWith(normalized)) 0 else 1
                }.thenBy { it.name }
            )
            .toList()
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
}
