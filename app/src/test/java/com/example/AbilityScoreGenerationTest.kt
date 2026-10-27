package com.example

import com.example.dnd35.model.PointBuyBudget
import com.example.dnd35.model.pointBuyCost
import com.example.dnd35.model.pointBuyTotalCost
import com.example.dnd35.model.rollAbilityScore
import com.example.dnd35.model.rollAbilityScoreSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AbilityScoreGenerationTest {

    @Test
    fun pointBuyCostMatchesTheDungeonMastersGuideTable() {
        assertEquals(0, pointBuyCost(8))
        assertEquals(2, pointBuyCost(10))
        assertEquals(6, pointBuyCost(14))
        // Acima de 14 o custo cresce mais rápido (15 a 18 custam mais por ponto).
        assertEquals(8, pointBuyCost(15))
        assertEquals(16, pointBuyCost(18))
    }

    @Test
    fun pointBuyCostIsNullOutsideTheValidRange() {
        assertNull(pointBuyCost(7))
        assertNull(pointBuyCost(19))
    }

    @Test
    fun pointBuyTotalCostSumsEachAbility() {
        // Seis atributos em 8 (o mínimo) não custam nada.
        assertEquals(0, pointBuyTotalCost(listOf(8, 8, 8, 8, 8, 8)))
        // 14 (6 pontos) nos seis atributos = 36 pontos, mais que o orçamento padrão de 25.
        assertEquals(36, pointBuyTotalCost(listOf(14, 14, 14, 14, 14, 14)))
        assertTrue(36 > PointBuyBudget.PADRAO.totalPoints)
    }

    @Test
    fun pointBuyTotalCostIsNullWhenAnyScoreIsOutOfRange() {
        assertNull(pointBuyTotalCost(listOf(8, 8, 8, 8, 8, 19)))
    }

    @Test
    fun rolledAbilityScoreIsAlwaysBetweenThreeAndEighteen() {
        val random = Random(42)
        repeat(200) {
            val score = rollAbilityScore(random)
            assertTrue("Atributo rolado fora de 3..18: $score", score in 3..18)
        }
    }

    @Test
    fun rolledAbilityScoreSetHasExactlySixValues() {
        val set = rollAbilityScoreSet(Random(7))
        assertEquals(6, set.size)
        assertTrue(set.all { it in 3..18 })
    }
}
