package com.example

import com.example.dnd35.viewmodel.convertCoinBalancesToGold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoinConversionTest {
    private fun copperEquivalent(gp: Float, sp: Float, cp: Float, pp: Float): Double =
        gp.toDouble() * 100.0 + sp.toDouble() * 10.0 + cp.toDouble() + pp.toDouble() * 1000.0

    @Test
    fun convertsPlatinumAndPreservesEquivalentValue() {
        val converted = convertCoinBalancesToGold(0f, 5f, 0f, 1f)!!
        assertEquals(10f, converted.goldGp, 0f)
        assertEquals(5f, converted.silverSp, 0f)
        assertEquals(0f, converted.copperCp, 0f)
        assertEquals(0f, converted.platinumPp, 0f)
        assertEquals(1050.0, copperEquivalent(converted.goldGp, converted.silverSp, converted.copperCp, converted.platinumPp), 0.0001)
    }

    @Test
    fun preservesFractionalCopperInsteadOfTruncatingIt() {
        val converted = convertCoinBalancesToGold(1.25f, 0f, 0f, 0f)!!
        assertEquals(1f, converted.goldGp, 0f)
        assertEquals(2f, converted.silverSp, 0f)
        assertEquals(5f, converted.copperCp, 0.0001f)
        assertEquals(125.0, copperEquivalent(converted.goldGp, converted.silverSp, converted.copperCp, converted.platinumPp), 0.0001)
    }

    @Test
    fun keepsFractionalCopperBalance() {
        val converted = convertCoinBalancesToGold(0f, 0f, 0.5f, 0f)!!
        assertEquals(0.5f, converted.copperCp, 0f)
    }

    @Test
    fun preservesValueAcrossManyWholeCoinCombinations() {
        for (gp in 0..20) {
            for (sp in 0..20) {
                for (cp in 0..20) {
                    for (pp in 0..5) {
                        val converted = convertCoinBalancesToGold(gp.toFloat(), sp.toFloat(), cp.toFloat(), pp.toFloat())!!
                        assertEquals(
                            copperEquivalent(gp.toFloat(), sp.toFloat(), cp.toFloat(), pp.toFloat()),
                            copperEquivalent(converted.goldGp, converted.silverSp, converted.copperCp, converted.platinumPp),
                            0.0001
                        )
                        assertEquals(0f, converted.platinumPp, 0f)
                    }
                }
            }
        }
    }

    @Test
    fun rejectsInvalidBalancesWithoutConversion() {
        assertNull(convertCoinBalancesToGold(-1f, 0f, 0f, 0f))
        assertNull(convertCoinBalancesToGold(Float.NaN, 0f, 0f, 0f))
        assertNull(convertCoinBalancesToGold(0f, 0f, Float.POSITIVE_INFINITY, 0f))
    }
}
