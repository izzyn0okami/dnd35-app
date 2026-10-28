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
 * Estima o total de pontos de perícia disponíveis ao personagem, somando automaticamente
 * do 1º nível até o nível atual — incluindo multiclasse — sem depender de nenhum registro
 * manual por nível. Usa o INT atual para todos os níveis (se o INT mudou ao longo da
 * carreira, o total não é recalculado retroativamente; é uma estimativa, como o resto
 * deste arquivo). Nulo se não houver classes ou alguma classe for desconhecida.
 *
 * Regra 3.5: o multiplicador x4 e o bônus racial de 1º nível (ex.: Humano +4) valem só
 * para o 1º nível do PERSONAGEM, não o 1º nível de cada classe tomada em multiclasse.
 * Por isso a primeira entrada de [classEntries] é tratada como a classe inicial — só o
 * primeiro nível dela recebe o multiplicador — e toda classe seguinte (e os demais
 * níveis da classe inicial) seguem a regra normal de nível seguinte.
 */
fun estimateTotalSkillPoints(
    classEntries: List<CharacterClassEntry>,
    intelligenceModifier: Int,
    raceName: String
): Int? {
    if (classEntries.isEmpty()) return null
    val human = raceName.equals("Humano", ignoreCase = true)
    var total = 0
    for ((index, entry) in classEntries.withIndex()) {
        val classInfo = Dnd35Rules.CLASSES.firstOrNull {
            it.name.equals(entry.className.trim(), ignoreCase = true)
        } ?: return null
        val perLevel = (classInfo.skillPointsPerLevel + intelligenceModifier).coerceAtLeast(1)
        val levels = entry.level.coerceAtLeast(0)
        if (levels == 0) continue
        if (index == 0) {
            // Classe inicial: 1º nível do personagem (multiplicado) + demais níveis dela (normais).
            total += perLevel * 4 + (if (human) 4 else 0)
            val remainingLevels = levels - 1
            total += remainingLevels * perLevel + remainingLevels * (if (human) 1 else 0)
        } else {
            // Classe tomada depois: todos os seus níveis seguem a regra de nível seguinte.
            total += levels * perLevel + levels * (if (human) 1 else 0)
        }
    }
    return total
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
        // Nomes personalizados são permitidos para campanhas e especialidades de Ofícios.
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
    val hasDuplicateLevels = currentLevelHistory
        .groupingBy { it.characterLevel }
        .eachCount()
        .values
        .any { it > 1 }
    // Dois registros para o mesmo nível tornam o histórico ambíguo: não
    // podemos somar ambos e apresentar o resultado como saldo confirmado.
    val complete = !hasDuplicateLevels && (1..totalCharacterLevel).all { it in recordedLevels }
    val spent = currentLevelHistory.sumOf { record ->
        record.allocations.sumOf { it.pointsSpent.toDouble() }
    }.toFloat()
    return SkillPointHistoryTotal(isComplete = complete, pointsSpent = spent)
}
