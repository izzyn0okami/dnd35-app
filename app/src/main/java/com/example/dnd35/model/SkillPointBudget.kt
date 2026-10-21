package com.example.dnd35.model

/**
 * Estima os pontos de perícia ganhos em um nível de personagem.
 *
 * A estimativa usa o modificador de Inteligência fornecido e o nome da raça.
 * Para registros históricos, esses valores podem ter sido diferentes; por isso
 * a função não altera nem valida automaticamente registros já salvos.
 */
fun estimateSkillPointsForLevel(
    className: String,
    characterLevel: Int,
    intelligenceModifier: Int,
    raceName: String
): Int? {
    if (characterLevel < 1) return null
    val classInfo = Dnd35Rules.CLASSES.firstOrNull {
        it.name.equals(className.trim(), ignoreCase = true)
    } ?: return null

    val human = raceName.equals("Humano", ignoreCase = true)
    val pointsBeforeLevelMultiplier = (classInfo.skillPointsPerLevel + intelligenceModifier).coerceAtLeast(1)
    val levelPoints = if (characterLevel == 1) pointsBeforeLevelMultiplier * 4 else pointsBeforeLevelMultiplier
    val racialBonus = if (human) {
        if (characterLevel == 1) 4 else 1
    } else 0

    return levelPoints + racialBonus
}
