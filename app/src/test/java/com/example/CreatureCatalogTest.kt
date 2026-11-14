package com.example

import com.example.dnd35.model.CreatureCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreatureCatalogTest {
    @Test
    fun catalogLoadsWithUniqueIdsAndValidSizes() {
        val all = CreatureCatalog.ALL
        assertTrue("Catálogo deveria ter ao menos 200 criaturas", all.size >= 200)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        all.forEach {
            assertTrue("Tamanho inválido em ${it.name}: ${it.size}", it.size in CreatureCatalog.SIZES)
            assertTrue("PV inválido em ${it.name}", it.hitPoints > 0)
        }
    }

    @Test
    fun wolfMatchesMonsterManual() {
        val wolf = CreatureCatalog.byId("lobo")
        assertNotNull(wolf)
        wolf!!
        assertEquals("2d8+4", wolf.hitDice)
        assertEquals(13, wolf.hitPoints)
        assertEquals(14, wolf.armorClass)
        assertEquals("Médio", wolf.size)
    }

    @Test
    fun owlHasFractionalChallengeRating() {
        val owl = CreatureCatalog.byId("coruja")!!
        assertEquals("1/4", owl.challengeRating)
        assertEquals(0.25, owl.challengeRatingValue, 0.0001)
        assertEquals("Miúdo", owl.size)
    }

    @Test
    fun searchIgnoresAccentsAndCombinesFilters() {
        val byAccent = CreatureCatalog.filter(query = "MEDIO")
        assertTrue(byAccent.isEmpty() || byAccent.all { CreatureCatalog.normalizeForSearch(it.name).contains("medio") })
        assertTrue(CreatureCatalog.filter(query = "lobo").any { it.id == "lobo" })
        val weak = CreatureCatalog.filter(maxChallengeRating = 1.0, size = "Médio")
        assertTrue(weak.isNotEmpty())
        assertTrue(weak.all { it.size == "Médio" && it.challengeRatingValue <= 1.0 })
    }

    @Test
    fun undeadStyleCreaturesHaveNullIntelligence() {
        assertTrue(CreatureCatalog.ALL.any { it.abilities.int == null })
    }

    @Test
    fun srdAnimalsFillTheCompanionAndSummonGaps() {
        val hyena = CreatureCatalog.byId("hiena")!!
        assertEquals(13, hyena.hitPoints)
        assertEquals(14, hyena.armorClass)
        assertEquals("1", hyena.challengeRating)
        assertTrue(hyena.source.startsWith("SRD 3.5"))

        val viper = CreatureCatalog.byId("vibora-media")!!
        assertEquals(9, viper.hitPoints)
        assertEquals(16, viper.armorClass)

        val scorpion = CreatureCatalog.byId("escorpiao-medio")!!
        assertEquals("2d8+4", scorpion.hitDice)
        assertEquals(null, scorpion.abilities.int)

        assertTrue(CreatureCatalog.byId("aranha-colossal") != null)
        assertTrue(CreatureCatalog.byId("centopeia-imensa") != null)
        assertTrue(CreatureCatalog.byId("tubarao-enorme") != null)
    }

    @Test
    fun rulesEligibilityFlagsMatchPlayersHandbookLists() {
        val wolf = CreatureCatalog.byId("lobo")!!
        assertTrue(wolf.animalCompanion)
        assertTrue(CreatureCatalog.byId("arminho-atroz")!!.animalCompanion)
        assertTrue(wolf.summonedCreature)
        assertTrue(!wolf.familiar)
        assertTrue(CreatureCatalog.byId("urso-atroz")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("elefante")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("polvo-gigante")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("lula-gigante")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("texugo-atroz")!!.animalCompanion)
        assertTrue(CreatureCatalog.byId("texugo-atroz")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("cobra-constritora-gigante")!!.animalCompanion)
        assertTrue(CreatureCatalog.byId("cobra-constritora-gigante")!!.summonedCreature)

        val owl = CreatureCatalog.byId("coruja")!!
        assertTrue(owl.animalCompanion)
        assertTrue(owl.summonedCreature)
        assertTrue(owl.familiar)

        val cat = CreatureCatalog.byId("gato")!!
        assertTrue(cat.familiar)
        assertTrue(!cat.animalCompanion)

        val aboleth = CreatureCatalog.byId("abolete")!!
        assertTrue(!aboleth.animalCompanion)
        assertTrue(!aboleth.summonedCreature)
        assertTrue(!aboleth.familiar)
    }


    @Test
    fun monstrousArthropodsInSummonMonsterListsAreMarked() {
        listOf(
            "centopeia-media", "centopeia-grande", "centopeia-enorme",
            "centopeia-imensa", "centopeia-colossal",
            "aranha-pequena", "aranha-media", "aranha-grande",
            "aranha-enorme", "aranha-imensa", "aranha-colossal",
            "escorpiao-medio", "escorpiao-grande", "escorpiao-enorme",
            "escorpiao-imenso"
        ).forEach { id ->
            assertTrue("$id deveria constar nas listas de invocação", CreatureCatalog.byId(id)!!.summonedCreature)
        }
        assertTrue("Escorpião Colossal não consta na tabela de Invocar Criaturas", !CreatureCatalog.byId("escorpiao-colossal")!!.summonedCreature)
    }

    @Test
    fun direAnimalsHaveNarrativeAndCombatSummaries() {
        listOf(
            "arminho-atroz", "carcaju-atroz", "gorila-atroz", "javali-atroz",
            "leao-atroz", "lobo-atroz", "morcego-atroz", "rato-atroz",
            "tigre-atroz", "tubarao-atroz", "urso-atroz"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
    }

    @Test
    fun earlyAlphabeticalCreaturesHaveNarrativeAndCombatSummaries() {
        listOf(
            "aranea", "aranha-interplanar", "arbusto-errante", "arconte-luminar",
            "arconte-mensageiro", "azer", "beholder", "belker", "anis", "bruxa-verde"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        val beholder = CreatureCatalog.byId("beholder")!!
        assertTrue(beholder.attack.contains("raios ópticos"))
        assertTrue(beholder.fullAttack.contains("raios ópticos"))
    }
}
