package com.example.dnd35.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyClassEntryCompatibilityTest {

    @Test
    fun legacyClassNameWithLevelSuffixIsCanonicalized() {
        val entries = CharacterJsonAdapters.deserializeClassEntries(
            """[{"className":"Guerreiro 4","level":4}]""",
            fallbackClass = "Guerreiro 4 4",
            fallbackLevel = 4
        )

        assertEquals(listOf(CharacterClassEntry("Guerreiro", 4)), entries)
    }

    @Test
    fun legacyClassNameWithRepeatedLevelSuffixIsCanonicalized() {
        val entries = CharacterJsonAdapters.deserializeClassEntries(
            """[{"className":"Guerreiro 4 4","level":4}]""",
            fallbackClass = "Guerreiro 4 4",
            fallbackLevel = 4
        )

        assertEquals(listOf(CharacterClassEntry("Guerreiro", 4)), entries)
    }

    @Test
    fun canonicalClassesAndMulticlassLevelsArePreserved() {
        val entries = CharacterJsonAdapters.deserializeClassEntries(
            """[{"className":"Guerreiro","level":4},{"className":"Ladino","level":2}]"""
        )

        assertEquals(
            listOf(CharacterClassEntry("Guerreiro", 4), CharacterClassEntry("Ladino", 2)),
            entries
        )
    }

    @Test
    fun unknownClassDoesNotBecomeFirstCatalogClass() {
        val entries = CharacterJsonAdapters.deserializeClassEntries(
            """[{"className":"Classe inexistente","level":4}]""",
            fallbackClass = "Guerreiro",
            fallbackLevel = 4
        )

        assertEquals(listOf(CharacterClassEntry("Guerreiro", 4)), entries)
    }

    @Test
    fun legacyFighterSheetDoesNotReceiveBarbarianFeatures() {
        val stats = CharacterCalculator.compute(
            CharacterEntity(
                characterClass = "Guerreiro 4 4",
                level = 4,
                multiclassJson = """[{"className":"Guerreiro 4","level":4}]"""
            )
        )

        assertEquals("Guerreiro", stats.classInfo.name)
        assertEquals(listOf(CharacterClassEntry("Guerreiro", 4)), stats.classEntries)
        assertTrue(stats.activeClassFeatures.any { it.name.contains("Talento", ignoreCase = true) })
        assertFalse(stats.activeClassFeatures.any {
            it.name.contains("Fúria", ignoreCase = true) ||
                it.name.contains("Movimento Rápido", ignoreCase = true) ||
                it.name.contains("Esquiva Sobrenatural", ignoreCase = true)
        })
    }

    @Test
    fun barbarianStillReceivesItsOwnFeatures() {
        val stats = CharacterCalculator.compute(
            CharacterEntity(
                characterClass = "Bárbaro",
                level = 4,
                multiclassJson = """[{"className":"Bárbaro","level":4}]"""
            )
        )

        assertEquals("Bárbaro", stats.classInfo.name)
        assertTrue(stats.activeClassFeatures.any { it.name.contains("Fúria", ignoreCase = true) })
        assertTrue(stats.activeClassFeatures.any { it.name.contains("Movimento Rápido", ignoreCase = true) })
    }
}
