package com.example.dnd35.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpellGlossarySearchTest {
    @Test
    fun searchesSpellNamesAndDescriptionsWithoutAccentSensitivity() {
        assertTrue(SpellGlossarySearch.search("misseis magicos").any { it.spell.name == "Mísseis Mágicos" })
        assertTrue(SpellGlossarySearch.search("energia").isNotEmpty())
    }

    @Test
    fun keepsSpellMechanicsSeparateFromGlossaryReferences() {
        val result = SpellGlossarySearch.search("Mísseis Mágicos").first()
        assertTrue(result.spell.level == 1)
        assertTrue(result.spell.castTime.isNotBlank())
        assertFalse(result.spell.description.isBlank())
        assertTrue(result.reference?.sourcePage == 220)
        assertTrue(result.reference?.explanation?.contains("projéteis") == true)

        val shield = SpellGlossarySearch.search("Escudo Arcano").first()
        assertTrue(shield.reference?.sourcePage == 200)
        assertTrue(shield.reference?.interactions?.contains("cobertura") == true)
    }
}
