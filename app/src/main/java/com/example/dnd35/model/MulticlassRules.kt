package com.example.dnd35.model

/**
 * Calcula a penalidade de XP por desequilíbrio entre classes não favorecidas.
 * Raças com classe favorecida "Qualquer" não recebem essa penalidade.
 */
data class MulticlassXpPenalty(
    val percentage: Int,
    val offendingClasses: List<CharacterClassEntry>
)

fun calculateMulticlassXpPenalty(
    classes: List<CharacterClassEntry>,
    favoredClass: String,
    anyClassFavored: Boolean = favoredClass.equals("Qualquer", ignoreCase = true)
): MulticlassXpPenalty {
    if (anyClassFavored) return MulticlassXpPenalty(0, emptyList())

    val nonFavored = classes.filterNot { it.className.equals(favoredClass, ignoreCase = true) }
    if (nonFavored.size <= 1) return MulticlassXpPenalty(0, emptyList())

    val highestLevel = nonFavored.maxOfOrNull { it.level } ?: return MulticlassXpPenalty(0, emptyList())
    val offending = nonFavored.filter { highestLevel - it.level >= 2 }
    return MulticlassXpPenalty(offending.size * 20, offending)
}
