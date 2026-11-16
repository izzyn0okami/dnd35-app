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

        val ghoulTouch = SpellGlossarySearch.search("Toque do Carniçal").first()
        assertTrue(ghoulTouch.reference?.sourcePage == 248)
        assertTrue(ghoulTouch.reference?.explanation?.contains("paralisa") == true)
        assertTrue(ghoulTouch.reference?.interactions?.contains("Neutralizar venenos") == true)

        val macabreTouch = SpellGlossarySearch.search("Toque Macabro").first()
        assertTrue(macabreTouch.reference?.sourcePage == 249)
        assertTrue(macabreTouch.reference?.explanation?.contains("1d6") == true)
        assertTrue(macabreTouch.reference?.tacticalNotes?.contains("dano temporário de Força") == true)
        assertTrue(macabreTouch.reference?.interactions?.contains("Fortitude parcial ou Vontade anula") == true)

        val vampiricTouch = SpellGlossarySearch.search("Toque Vampírico").first()
        assertTrue(vampiricTouch.reference?.sourcePage == 249)
        assertTrue(vampiricTouch.reference?.explanation?.contains("10d6") == true)
        assertTrue(vampiricTouch.reference?.tacticalNotes?.contains("1 hora") == true)

        val rustingGrasp = SpellGlossarySearch.search("Toque Enferrujante").first()
        assertTrue(rustingGrasp.reference?.sourcePage == 249)
        assertTrue(rustingGrasp.reference?.tacticalNotes?.contains("3d6") == true)
        assertTrue(rustingGrasp.reference?.interactions?.contains("não admite Resistência à Magia") == true)

        val touchOfIdiocy = SpellGlossarySearch.search("Toque da Idiotice").first()
        assertTrue(touchOfIdiocy.reference?.sourcePage == 249)
        assertTrue(touchOfIdiocy.reference?.explanation?.contains("1d6") == true)
        assertTrue(touchOfIdiocy.reference?.tacticalNotes?.contains("abaixo de 1") == true)

        val dimensional = SpellGlossarySearch.search("Tranca Dimensional").first()
        assertTrue(dimensional.reference?.sourcePage == 250)
        assertTrue(dimensional.reference?.interactions?.contains("não admite Resistência à Magia") == true)

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
        assertTrue(darkvision.reference?.tacticalNotes?.contains("escuridão mágica") == true)

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
        assertTrue(planar.reference?.explanation?.contains("750 km") == true)

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
        assertTrue(falseVision.reference?.explanation?.contains("12 m") == true)

        val haste = SpellGlossarySearch.search("Velocidade").first()
        assertTrue(haste.reference?.sourcePage == 250)
        assertTrue(haste.reference?.tacticalNotes?.contains("+1") == true)
        assertTrue(haste.reference?.interactions?.contains("dissipa lentidão") == true)

        val vision = SpellGlossarySearch.search("Visão").first()
        assertTrue(vision.reference?.sourcePage == 252)
        assertTrue(vision.reference?.tacticalNotes?.contains("CD é 30") == true)
        assertTrue(vision.reference?.interactions?.contains("100 XP") == true)
    }

    @Test
    fun auditsAlphabeticalSpellBatchAAgainstBrazilianPlayerHandbook() {
        val holyWater = SpellGlossarySearch.search("Abençoar Água").first()
        assertTrue(holyWater.reference?.sourcePage == 168)
        assertTrue(holyWater.reference?.explanation?.contains("0,5 litro") == true)
        assertTrue(holyWater.reference?.interactions?.contains("Vontade anula (objeto)") == true)
        assertTrue(holyWater.spell.school.contains("[Bem]"))

        val blessWeapon = SpellGlossarySearch.search("Abençoar Arma").first()
        assertTrue(blessWeapon.reference?.sourcePage == 168)
        assertTrue(blessWeapon.reference?.tacticalNotes?.contains("ameaças de acerto crítico") == true)
        assertTrue(blessWeapon.reference?.interactions?.contains("não admite Resistência à Magia") == true)

        val openClose = SpellGlossarySearch.search("Abrir/Fechar").first()
        assertTrue(openClose.reference?.sourcePage == 168)
        assertTrue(openClose.reference?.tacticalNotes?.contains("15 kg") == true)
        assertTrue(openClose.spell.range.contains("1,5 m/2 níveis"))

        val calmAnimals = SpellGlossarySearch.search("Acalmar Animais").first()
        assertTrue(calmAnimals.reference?.sourcePage == 168)
        assertTrue(calmAnimals.reference?.explanation?.contains("Inteligência 1 ou 2") == true)
        assertTrue(calmAnimals.reference?.interactions?.contains("Qualquer ameaça") == true)
        assertTrue(calmAnimals.spell.school.contains("(Compulsão) [Ação Mental]"))

        val calmEmotions = SpellGlossarySearch.search("Acalmar Emoções").first()
        assertTrue(calmEmotions.reference?.sourcePage == 168)
        assertTrue(calmEmotions.reference?.tacticalNotes?.contains("efeitos de medo") == true)

        val divination = SpellGlossarySearch.search("Adivinhação").first()
        assertTrue(divination.reference?.sourcePage == 169)
        assertTrue(divination.reference?.tacticalNotes?.contains("70%") == true)
        assertTrue(divination.reference?.tacticalNotes?.contains("25 PO") == true)

        val catsGrace = SpellGlossarySearch.search("Agilidade do Gato").first()
        assertTrue(catsGrace.reference?.sourcePage == 169)
        assertTrue(catsGrace.reference?.explanation?.contains("+4") == true)
        assertTrue(catsGrace.reference?.interactions?.contains("Vontade anula (inofensiva)") == true)

        val aid = SpellGlossarySearch.search("Ajuda").first()
        assertTrue(aid.reference?.sourcePage == 169)
        assertTrue(aid.reference?.explanation?.contains("1d8") == true)
        assertTrue(aid.reference?.tacticalNotes?.contains("+10") == true)

        val alarm = SpellGlossarySearch.search("Alarme").first()
        assertTrue(alarm.reference?.sourcePage == 169)
        assertTrue(alarm.reference?.explanation?.contains("6 m de raio") == true)
        assertTrue(alarm.reference?.interactions?.contains("Permanência") == true)
    }


    @Test
    fun auditsAlphabeticalSpellBatchAmAgainstBrazilianPlayerHandbook() {
        val cursedWater = SpellGlossarySearch.search("Amaldiçoar Água").first()
        assertEquals(170, cursedWater.reference?.sourcePage)
        assertTrue(cursedWater.reference?.explanation?.contains("500 ml") == true)
        assertTrue(cursedWater.reference?.tacticalNotes?.contains("2,5 kg") == true)
        assertTrue(cursedWater.spell.school.contains("Necromancia [Mal]"))

        val softenEarth = SpellGlossarySearch.search("Amolecer Terra e Pedra").first()
        assertEquals(170, softenEarth.reference?.sourcePage)
        assertTrue(softenEarth.reference?.tacticalNotes?.contains("1d2 rodadas") == true)
        assertTrue(softenEarth.reference?.interactions?.contains("Reflexos") == true)
        assertTrue(softenEarth.reference?.interactions?.contains("Resistência à Magia: Não") == true)

        val animalGrowth = SpellGlossarySearch.search("Ampliar Animais").first()
        assertEquals(170, animalGrowth.reference?.sourcePage)
        assertTrue(animalGrowth.reference?.tacticalNotes?.contains("10/mágica") == true)
        assertTrue(animalGrowth.reference?.tacticalNotes?.contains("não se acumulam") == true)

        val plantGrowth = SpellGlossarySearch.search("Ampliar Plantas").first()
        assertEquals(171, plantGrowth.reference?.sourcePage)
        assertTrue(plantGrowth.reference?.explanation?.contains("750 km") == true)
        assertTrue(plantGrowth.reference?.tacticalNotes?.contains("30 m de raio") == true)
        assertTrue(plantGrowth.reference?.interactions?.contains("Enfraquecer Plantas") == true)
    }

}
