package com.example.dnd35.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LatestCalculationGateTest {
    @Test
    fun newerRequestInvalidatesOlderCalculation() {
        val gate = LatestCalculationGate()
        val first = gate.begin()
        val second = gate.begin()
        assertFalse(gate.isCurrent(first))
        assertTrue(gate.isCurrent(second))
    }

    @Test
    fun currentRequestRemainsValidUntilAnotherStarts() {
        val gate = LatestCalculationGate()
        val request = gate.begin()
        assertTrue(gate.isCurrent(request))
    }
}
