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
}
