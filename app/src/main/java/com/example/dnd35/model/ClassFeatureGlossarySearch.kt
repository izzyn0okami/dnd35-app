package com.example.dnd35.model

import java.text.Normalizer
import java.util.Locale

/**
 * Índice textual das habilidades de classe para consulta contextual.
 *
 * Este componente pesquisa somente nomes e descrições estáticas. Não recebe ficha,
 * nível efetivo, atributos ou estatísticas calculadas e não participa das regras mecânicas.
 */
data class ClassFeatureGlossaryResult(
    val className: String,
    val level: Int,
    val term: String,
    val explanation: String
)

object ClassFeatureGlossarySearch {
    fun search(query: String): List<ClassFeatureGlossaryResult> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()

        return Dnd35Rules.CLASSES
            .flatMap { classInfo ->
                classInfo.features.map { feature ->
                    ClassFeatureGlossaryResult(
                        className = classInfo.name,
                        level = feature.level,
                        term = feature.name,
                        explanation = feature.description
                    )
                }
            }
            .filter { result ->
                normalize(result.term).contains(normalizedQuery) ||
                    normalize(result.explanation).contains(normalizedQuery)
            }
            .sortedWith(
                compareBy<ClassFeatureGlossaryResult> {
                    if (normalize(it.term) == normalizedQuery) 0 else 1
                }.thenBy {
                    if (normalize(it.term).startsWith(normalizedQuery)) 0 else 1
                }.thenBy { it.className }.thenBy { it.level }
            )
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .trim()
}
