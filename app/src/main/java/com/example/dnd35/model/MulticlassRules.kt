package com.example.dnd35.model

/**
 * Calcula a penalidade de XP conforme a regra de multiclasse do Livro do Jogador.
 * Para raças com classe favorecida "Qualquer", uma única classe — a de nível
 * mais alto — é desconsiderada no cálculo; as demais ainda podem gerar penalidade.
 */
data class MulticlassXpPenalty(
    val percentage: Int,
    val offendingClasses: List<CharacterClassEntry>,
    val exemptClassName: String? = null
)

fun calculateMulticlassXpPenalty(
    classes: List<CharacterClassEntry>,
    favoredClass: String,
    anyClassFavored: Boolean = favoredClass.equals("Qualquer", ignoreCase = true)
): MulticlassXpPenalty {
    val exemptClass = if (anyClassFavored) classes.maxByOrNull { it.level } else null
    val exemptName = exemptClass?.className
    val nonFavored = if (anyClassFavored) {
        classes.filterNot { it.className == exemptName }
    } else {
        classes.filterNot { it.className.equals(favoredClass, ignoreCase = true) }
    }
    if (nonFavored.size <= 1) return MulticlassXpPenalty(0, emptyList(), exemptName)

    val highestLevel = nonFavored.maxOfOrNull { it.level }
        ?: return MulticlassXpPenalty(0, emptyList(), exemptName)
    val offending = nonFavored.filter { highestLevel - it.level >= 2 }
    return MulticlassXpPenalty(offending.size * 20, offending, exemptName)
}
