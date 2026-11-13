package com.example

import com.example.dnd35.model.SummoningFamily
import com.example.dnd35.model.SummoningRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SummoningRulesTest {
    @Test
    fun summonCreatureListsIncludeLowerCircleOptions() {
        val circle1 = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_CREATURES, 1)
        val circle3 = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_CREATURES, 3)
        assertTrue(circle1.isNotEmpty())
        assertTrue(circle1.all { it.sourceCircle == 1 })
        assertTrue(circle3.any { it.sourceCircle == 1 })
        assertTrue(circle3.any { it.sourceCircle == 2 })
        assertTrue(circle3.any { it.sourceCircle == 3 })
        assertFalse(circle3.any { it.sourceCircle > 3 })
    }

    @Test
    fun natureAllyListsAreSeparateFromSummonMonsterLists() {
        val nature1 = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 1)
        assertTrue(nature1.any { it.creature.id == "lobo" })
        assertTrue(nature1.any { it.creature.id == "rato-atroz" })
        assertFalse(nature1.any { it.creature.id == "leao-celestial" })
    }

    @Test
    fun classRestrictionsFollowSpellLists() {
        assertTrue(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_CREATURES, "Mago"))
        assertTrue(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_CREATURES, "Clérigo"))
        assertFalse(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_CREATURES, "Druida"))
        assertTrue(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_NATURE_ALLY, "Druida"))
        assertTrue(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_NATURE_ALLY, "Ranger"))
        assertFalse(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_NATURE_ALLY, "Mago"))
    }

    @Test
    fun everySummonFlagHasAnExplicitBookListCircle() {
        assertEquals(emptyList<String>(), SummoningRules.unclassifiedCatalogCreatureIds())
        assertFalse(com.example.dnd35.model.CreatureCatalog.byId("escorpiao-colossal")!!.summonedCreature)
    }

    @Test
    fun animalDomainProvidesNatureAllySpellsToClerics() {
        val animalDomainSpells = com.example.dnd35.model.ClericDomainRules.domainSpellsAt(
            listOf("Animais"),
            1
        )
        assertTrue(animalDomainSpells.any { it.second.name == "Invocar Aliado da Natureza I" })
        assertFalse(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_NATURE_ALLY, "Clérigo"))
    }

    @Test
    fun templatedSummonsAreMarkedInOptions() {
        val dog = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_CREATURES, 1)
            .first { it.creature.id == "cachorro" }
        assertEquals("Celestial", dog.templateLabel)
        val fiendishRat = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_CREATURES, 1)
            .first { it.creature.id == "rato-atroz-abissal" }
        assertEquals("", fiendishRat.templateLabel)
    }
}
