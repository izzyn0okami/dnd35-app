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


/**
 * Analisa gastos por perícia para um novo registro de nível.
 * Pontos de perícia são inteiros; graduações fracionárias não significam
 * gasto fracionário (uma perícia entreclasse consome 1 ponto para meia graduação).
 * Retorna null quando houver linha inválida ou perícias repetidas.
 */
fun parseSkillPointAllocations(input: String): List<SkillPointAllocation>? {
    val lines = input.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val parsed = mutableListOf<SkillPointAllocation>()
    val names = mutableSetOf<String>()

    for (line in lines) {
        val parts = line.split("=", limit = 2)
        if (parts.size != 2) return null
        val suppliedName = parts[0].trim()
        val name = Dnd35Rules.canonicalSkillName(suppliedName)
        val points = parts[1].trim().toIntOrNull()
        if (name.isEmpty() || points == null || points < 0) return null
        // Nomes antigos conhecidos (por exemplo, "Salto" e "Ofício") não
        // podem contornar a validação de duplicatas no mesmo nível.
        if (!names.add(name.lowercase())) return null
        parsed += SkillPointAllocation(name, points.toFloat())
    }
    return parsed
}

/** Aceita apenas orçamentos inteiros não negativos para novos registros. */
fun parseWholeSkillPointBudget(input: String): Int? {
    val value = input.trim().toIntOrNull() ?: return null
    return value.takeIf { it >= 0 }
}


/**
 * Calcula o total de gastos registrados somente para os níveis atuais do personagem.
 * Registros acima do nível atual são preservados, mas não entram no total nem
 * impedem que o histórico dos níveis atuais seja considerado completo.
 */
data class SkillPointHistoryTotal(
    val isComplete: Boolean,
    val pointsSpent: Float
)

fun calculateSkillPointHistoryTotal(
    history: List<SkillPointLevelRecord>,
    totalCharacterLevel: Int
): SkillPointHistoryTotal {
    if (totalCharacterLevel <= 0) {
        return SkillPointHistoryTotal(isComplete = false, pointsSpent = 0f)
    }
    val currentLevelHistory = history.filter { it.characterLevel in 1..totalCharacterLevel }
    val recordedLevels = currentLevelHistory.map { it.characterLevel }.toSet()
    val complete = (1..totalCharacterLevel).all { it in recordedLevels }
    val spent = currentLevelHistory.sumOf { record ->
        record.allocations.sumOf { it.pointsSpent.toDouble() }
    }.toFloat()
    return SkillPointHistoryTotal(isComplete = complete, pointsSpent = spent)
}
