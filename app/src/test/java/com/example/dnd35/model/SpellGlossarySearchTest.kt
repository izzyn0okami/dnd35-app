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

        val darkvision = SpellGlossarySearch.search("Visão no Escuro").first()
        assertTrue(darkvision.reference?.sourcePage == 252)
        assertTrue(darkvision.reference?.explanation?.contains("18 m") == true)
        assertTrue(darkvision.reference?.interactions?.contains("escuridão mágica") == true)

        val trueSeeing = SpellGlossarySearch.search("Visão da Verdade").first()
        assertTrue(trueSeeing.reference?.sourcePage == 253)
        assertTrue(trueSeeing.reference?.explanation?.contains("36 m") == true)
        assertTrue(trueSeeing.reference?.interactions?.contains("objetos sólidos") == true)

        val ventriloquism = SpellGlossarySearch.search("Ventriloquismo").first()
        assertTrue(ventriloquism.reference?.sourcePage == 251)
        assertTrue(ventriloquism.reference?.interactions?.contains("Resistência à Magia não se aplica") == true)

        val planar = SpellGlossarySearch.search("Viagem Planar").first()
        assertTrue(planar.reference?.sourcePage == 251)
        assertTrue(planar.reference?.tacticalNotes?.contains("oito criaturas") == true)
        assertTrue(planar.reference?.interactions?.contains("750 km") == true)

        val greaterScrying = SpellGlossarySearch.search("Vidência Maior").first()
        assertTrue(greaterScrying.reference?.sourcePage == 252)
        assertTrue(greaterScrying.reference?.explanation?.contains("1 hora por nível") == true)

        val greaterArcaneSight = SpellGlossarySearch.search("Visão Arcana Maior").first()
        assertTrue(greaterArcaneSight.reference?.interactions?.contains("não pode se tornar permanente") == true)

        val deathwatch = SpellGlossarySearch.search("Visão da Morte").first()
        assertTrue(deathwatch.reference?.explanation?.contains("3 pontos de vida ou menos") == true)
        assertTrue(deathwatch.reference?.interactions?.contains("Não permite teste de resistência") == true)

        val bearsEndurance = SpellGlossarySearch.search("Vigor do Urso").first()
        assertTrue(bearsEndurance.reference?.explanation?.contains("+4") == true)
        assertTrue(bearsEndurance.reference?.interactions?.contains("não são pontos de vida temporários") == true)

        val virtue = SpellGlossarySearch.search("Virtude").first()
        assertTrue(virtue.reference?.explanation?.contains("1 ponto de vida temporário") == true)

        val falseVision = SpellGlossarySearch.search("Visão Falsa").first()
        assertTrue(falseVision.reference?.sourcePage == 252)
        assertTrue(falseVision.reference?.tacticalNotes?.contains("12 m") == true)
    }
}
