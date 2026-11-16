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

        val hands = SpellGlossarySearch.search("Mãos Flamejantes").first()
        assertTrue(hands.reference?.sourcePage == 217)
        assertTrue(hands.reference?.explanation?.contains("5d4") == true)
        assertTrue(hands.reference?.interactions?.contains("Resistência à Magia") == true)

        val shocking = SpellGlossarySearch.search("Toque Chocante").first()
        assertTrue(shocking.reference?.sourcePage == 248)
        assertTrue(shocking.reference?.explanation?.contains("5d6") == true)
        assertTrue(shocking.reference?.tacticalNotes?.contains("+3") == true)

        val fatigue = SpellGlossarySearch.search("Toque da Fadiga").first()
        assertTrue(fatigue.reference?.sourcePage == 249)
        assertTrue(fatigue.reference?.interactions?.contains("Fortitude anula") == true)
        assertTrue(fatigue.reference?.interactions?.contains("gota de suor") == true)

        val dimensional = SpellGlossarySearch.search("Tranca Dimensional").first()
        assertTrue(dimensional.reference?.sourcePage == 250)
        assertTrue(dimensional.reference?.interactions?.contains("Não admite Resistência à Magia") == true)

        val disguise = SpellGlossarySearch.search("Transformação Momentânea").first()
        assertTrue(disguise.reference?.sourcePage == 250)
        assertTrue(disguise.reference?.tacticalNotes?.contains("+10") == true)

        val flight = SpellGlossarySearch.search("Voo").first()
        assertTrue(flight.reference?.sourcePage == 253)
        assertTrue(flight.reference?.explanation?.contains("18 m") == true)
        assertTrue(flight.reference?.interactions?.contains("1d6 rodadas") == true)

        val truth = SpellGlossarySearch.search("Zona da Verdade").first()
        assertTrue(truth.reference?.sourcePage == 253)
        assertTrue(truth.reference?.explanation?.contains("6 m") == true)
        assertTrue(truth.reference?.interactions?.contains("Vontade anula") == true)
    }
}
