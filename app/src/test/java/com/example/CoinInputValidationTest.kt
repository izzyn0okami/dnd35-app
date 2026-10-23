package com.example

import com.example.dnd35.ui.tabs.parseNonNegativeCoinAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoinInputValidationTest {
    @Test fun acceptsIntegerAndDecimalAmounts() {
        assertEquals(12f, parseNonNegativeCoinAmount("12")!!, 0f)
        assertEquals(1.5f, parseNonNegativeCoinAmount("1.5")!!, 0f)
    }

    @Test fun acceptsBrazilianDecimalCommaAndSurroundingSpaces() {
        assertEquals(2.75f, parseNonNegativeCoinAmount(" 2,75 ")!!, 0f)
    }

    @Test fun rejectsBlankMalformedNegativeAndNonFiniteAmounts() {
        listOf("", "   ", "abc", "-1", "NaN", "Infinity", "1,2,3").forEach {
            assertNull("Expected invalid amount for '$it'", parseNonNegativeCoinAmount(it))
        }
    }
}
