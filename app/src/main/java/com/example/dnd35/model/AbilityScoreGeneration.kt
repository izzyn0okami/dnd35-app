package com.example.dnd35.model

import kotlin.random.Random

/**
 * Métodos de geração dos seis atributos base oferecidos na criação guiada de personagem.
 * O app é um "companion": a rolagem de dados é sempre opcional, nunca obrigatória — o
 * jogador pode digitar o resultado dos próprios dados físicos em vez de rolar no app.
 */
enum class AbilityScoreMethod { ROLL_OR_MANUAL, POINT_BUY }

/**
 * Variantes de "compra por pontos" do Guia do Mestre 3.5 (regra opcional, DMG p. 169).
 * Não é uma regra do Livro do Jogador, mas é o jeito mais comum de gerar atributos sem
 * depender só de rolagem; oferecida como alternativa, não como obrigação.
 */
enum class PointBuyBudget(val totalPoints: Int, val label: String) {
    BAIXA_FANTASIA(15, "Baixa fantasia (15 pontos)"),
    PADRAO(25, "Padrão (25 pontos)"),
    ALTA_FANTASIA(32, "Alta fantasia (32 pontos)")
}

private val POINT_BUY_COST_TABLE: Map<Int, Int> = mapOf(
    8 to 0, 9 to 1, 10 to 2, 11 to 3, 12 to 4, 13 to 5,
    14 to 6, 15 to 8, 16 to 10, 17 to 13, 18 to 16
)

/** Custo em pontos para alcançar [score] na regra de compra por pontos. Nulo fora de 8..18. */
fun pointBuyCost(score: Int): Int? = POINT_BUY_COST_TABLE[score]

/** Custo total para o conjunto de seis atributos base. Nulo se algum valor estiver fora de 8..18. */
fun pointBuyTotalCost(scores: List<Int>): Int? {
    var total = 0
    for (score in scores) {
        total += pointBuyCost(score) ?: return null
    }
    return total
}

/** Rola 4d6 e descarta o menor resultado (método clássico de geração de um atributo). */
fun rollAbilityScore(random: Random = Random.Default): Int {
    val rolls = (1..4).map { random.nextInt(1, 7) }
    return rolls.sorted().drop(1).sum()
}

/** Rola os seis atributos base (em nenhuma ordem fixa; cabe ao jogador distribuí-los). */
fun rollAbilityScoreSet(random: Random = Random.Default): List<Int> =
    (1..6).map { rollAbilityScore(random) }
