package com.example.dnd35.session

import com.example.dnd35.model.CharacterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionCurrencyTest {
    @Test
    fun exchangeRatesAreConvertedToCopperExactly() {
        val character = CharacterEntity(goldGp = 1f, silverSp = 2f, copperCp = 3f, platinumPp = 1f)
        assertEquals(1123L, SessionCurrency.totalCopper(character))
    }

    @Test
    fun debitUsesAllDenominationsAndReturnsExactRemainder() {
        val character = CharacterEntity(goldGp = 1f, silverSp = 2f, copperCp = 3f, platinumPp = 1f)
        val purse = SessionCurrency.debit(character, 1.13f)
        assertEquals(SessionCoinPurse(0f, 1f, 0f, 1f), purse)
    }

    @Test
    fun insufficientFundsAreRejected() {
        val character = CharacterEntity(goldGp = 0f, silverSp = 1f, copperCp = 0f, platinumPp = 0f)
        assertNull(SessionCurrency.debit(character, 0.11f))
    }

    @Test
    fun subCopperPricesAreRejectedInsteadOfRounded() {
        assertNull(SessionCurrency.priceToCopper(0.001f))
    }

    @Test
    fun negativeAndSubCopperBalancesAreRejected() {
        assertNull(SessionCurrency.totalCopper(CharacterEntity(goldGp = -1f)))
        assertNull(SessionCurrency.totalCopper(CharacterEntity(goldGp = 0.001f)))
        assertEquals(150L, SessionCurrency.totalCopper(CharacterEntity(goldGp = 1.5f)))
    }

    @Test
    fun nonFinitePricesAreRejected() {
        assertNull(SessionCurrency.priceToCopper(Float.NaN))
        assertNull(SessionCurrency.priceToCopper(Float.POSITIVE_INFINITY))
    }
}
