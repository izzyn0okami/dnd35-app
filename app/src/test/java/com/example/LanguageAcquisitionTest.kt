package com.example

import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.LanguageAcquisition
import com.example.dnd35.model.LanguageAcquisitionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageAcquisitionTest {
    @Test
    fun purchasedLanguageRequiresLevelAndExactlyOnePoint() {
        assertTrue(LanguageAcquisition("Dracônico", LanguageAcquisitionSource.PURCHASED, 2, 1).isValid())
        assertTrue(!LanguageAcquisition("Dracônico", LanguageAcquisitionSource.PURCHASED, null, 1).isValid())
        assertTrue(!LanguageAcquisition("Dracônico", LanguageAcquisitionSource.PURCHASED, 2, 0).isValid())
        assertTrue(!LanguageAcquisition("Dracônico", LanguageAcquisitionSource.PURCHASED, 0, 1).isValid())
    }

    @Test
    fun freeLanguageSourcesCannotSpendSkillPoints() {
        assertTrue(LanguageAcquisition("Élfico", LanguageAcquisitionSource.RACIAL, null, 0).isValid())
        assertTrue(!LanguageAcquisition("Élfico", LanguageAcquisitionSource.RACIAL, 1, 1).isValid())
    }

    @Test
    fun serializationRoundTripsAndDeduplicatesLanguageNames() {
        val json = CharacterJsonAdapters.serializeLanguageAcquisitions(
            listOf(
                LanguageAcquisition("Dracônico", LanguageAcquisitionSource.PURCHASED, 3, 1),
                LanguageAcquisition("dracônico", LanguageAcquisitionSource.OTHER)
            )
        )
        assertEquals(
            listOf(LanguageAcquisition("Dracônico", LanguageAcquisitionSource.PURCHASED, 3, 1)),
            CharacterJsonAdapters.deserializeLanguageAcquisitions(json)
        )
    }

    @Test
    fun oldEmptyAcquisitionHistoryStaysEmptyRatherThanInferringPurchases() {
        assertTrue(CharacterJsonAdapters.deserializeLanguageAcquisitions("").isEmpty())
        assertTrue(CharacterJsonAdapters.deserializeLanguageAcquisitions(null).isEmpty())
    }
}
