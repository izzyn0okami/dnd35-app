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
    fun natureSummoningVariantsAppearAtTheirCorrectCircles() {
        val sixth = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 6)
        val seventh = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 7)
        val ninth = SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 9)
        assertTrue(SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 2).any { it.creature.id == "texugo-atroz" })
        assertTrue(SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 5).any { it.creature.id == "cobra-constritora-gigante" })
        assertTrue(SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_CREATURES, 3).any { it.creature.id == "texugo-atroz" })
        assertTrue(SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_CREATURES, 6).any { it.creature.id == "cobra-constritora-gigante" })
        assertTrue(sixth.any { it.creature.id == "urso-atroz" })
        assertTrue(sixth.any { it.creature.id == "polvo-gigante" })
        assertTrue(seventh.any { it.creature.id == "lula-gigante" })
        assertTrue(seventh.any { it.creature.id == "tiranossauro" })
        assertTrue(SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 4).any { it.creature.id == "leao-marinho" })
        assertTrue(SummoningRules.creaturesForCircle(SummoningFamily.SUMMON_NATURE_ALLY, 5).any { it.creature.id == "janni" })
        assertTrue(ninth.any { it.creature.id == "grig" })
        assertTrue(ninth.any { it.creature.id == "pixie" && it.templateLabel.contains("memória") })
        assertEquals(3, ninth.count { it.creature.id == "pixie" })
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
        val animalDomainSpells4 = com.example.dnd35.model.ClericDomainRules.domainSpellsAt(
            listOf("Animais"),
            4
        )
        val animalDomainSpells8 = com.example.dnd35.model.ClericDomainRules.domainSpellsAt(
            listOf("Animais"),
            8
        )
        assertTrue(animalDomainSpells4.any { it.second.name == "Invocar Aliado da Natureza IV" })
        assertTrue(animalDomainSpells8.any { it.second.name == "Invocar Aliado da Natureza VIII" })
        assertFalse(SummoningRules.isClassAllowed(SummoningFamily.SUMMON_NATURE_ALLY, "Clérigo"))
        val chaosDomainSpells9 = com.example.dnd35.model.ClericDomainRules.domainSpellsAt(listOf("Caos"), 9)
        assertTrue(chaosDomainSpells9.any { it.second.name == "Invocar Criaturas IX" })
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
