package com.example

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.session.SessionCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class SessionCurrencyTest {
    @Test
    fun debitsSeveralUnitsUsingEveryDenomination() {
        val character = CharacterEntity(
            goldGp = 1f,
            silverSp = 1f,
            copperCp = 1f,
            platinumPp = 1f
        )

        val purse = SessionCurrency.debit(character, costGp = 0.51f, quantity = 2)!!

        assertEquals(1f, purse.platinumPp, 0f)
        assertEquals(0f, purse.goldGp, 0f)
        assertEquals(0f, purse.silverSp, 0f)
        assertEquals(9f, purse.copperCp, 0f)
        assertEquals(1111L - 102L, SessionCurrency.totalCopper(purse.toCharacterUpdate(character))!!)
        assertEquals(1111L, SessionCurrency.totalCopper(character)!!)
    }

    @Test
    fun rejectsQuantityOutsideValidRangeAndInsufficientFunds() {
        val character = CharacterEntity(goldGp = 1f)
        assertNull(SessionCurrency.debit(character, 0.5f, quantity = 0))
        assertNull(SessionCurrency.debit(character, 0.5f, quantity = 3))
        assertNull(SessionCurrency.debit(character, 2f, quantity = 1))
    }

    @Test
    fun acceptsOnlyWholeCopperPieceAmounts() {
        assertTrue(SessionCurrency.isValidGpAmount(0.01f))
        assertTrue(SessionCurrency.isValidGpAmount(1.25f))
        assertFalse(SessionCurrency.isValidGpAmount(0.005f))
    }

    @Test
    fun creditsLootGoldAndNormalizesAllDenominations() {
        val character = CharacterEntity(goldGp = 1f, silverSp = 2f, copperCp = 5f, platinumPp = 0f)
        val purse = SessionCurrency.credit(character, 15.25f)!!

        assertEquals(1f, purse.platinumPp, 0f)
        assertEquals(6f, purse.goldGp, 0f)
        assertEquals(5f, purse.silverSp, 0f)
        assertEquals(0f, purse.copperCp, 0f)
    }
}
