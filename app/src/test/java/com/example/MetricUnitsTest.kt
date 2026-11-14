package com.example

import com.example.dnd35.model.MetricUnits
import org.junit.Assert.assertEquals
import org.junit.Test

class MetricUnitsTest {
    @Test
    fun convertsPoundsAndKilogramsWithoutChangingStoredUnitSemantics() {
        assertEquals(1f, MetricUnits.poundsToKilograms(2.2046226f), 0.0001f)
        assertEquals(2.2046226f, MetricUnits.kilogramsToPounds(1f), 0.0001f)
    }

    @Test
    fun convertsCoinCountUsingBrazilianPlayersHandbookWeight() {
        assertEquals(2.2046226f, MetricUnits.coinsToPounds(100f), 0.0001f)
        assertEquals(1.1023113f, MetricUnits.coinsToPounds(50f), 0.0001f)
    }

    @Test
    fun convertsFeetAndInchesToMetricUnits() {
        assertEquals(1f, MetricUnits.feetToMeters(3.2808399f), 0.0001f)
        assertEquals(18.288f, MetricUnits.feetToMeters(60f), 0.0001f)
        assertEquals(2.54f, MetricUnits.inchesToCentimeters(1f), 0.0001f)
    }
}
