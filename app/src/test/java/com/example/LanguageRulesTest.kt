package com.example

import com.example.dnd35.model.LanguageRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageRulesTest {
    @Test
    fun knownCoreRacesHaveExpectedAutomaticLanguages() {
        assertEquals(listOf("Comum"), LanguageRules.forRace("Humano")?.automatic)
        assertEquals(listOf("Comum", "Élfico"), LanguageRules.forRace("Elfo")?.automatic)
        assertEquals(listOf("Comum", "Orc"), LanguageRules.forRace("Meio-Orc")?.automatic)
        assertEquals(listOf("Comum", "Gnomo"), LanguageRules.forRace("Gnomo da Rocha")?.automatic)
    }

    @Test
    fun drowLanguagesMatchMonsterManualRulesAndRaceAlias() {
        val expected = LanguageRules.RaceLanguages(
            automatic = listOf("Comum", "Élfico", "Subterrâneo"),
            bonusOptions = listOf(
                "Abissal", "Aquático", "Dracônico", "Linguagem de Sinais Drow",
                "Gnomo", "Goblin", "Kuo-toa"
            )
        )
        assertEquals(expected, LanguageRules.forRace("Drow"))
        assertEquals(expected, LanguageRules.forRace("Drow (Elfo Negro)"))
    }

    @Test
    fun elfBonusLanguageChoicesMatchCoreRulesForElfVariants() {
        val expected = setOf("Dracônico", "Gnoll", "Gnomo", "Goblin", "Orc", "Silvestre")
        listOf("Elfo", "Alto Elfo", "Elfo da Floresta").forEach { race ->
            assertEquals(expected, LanguageRules.forRace(race)?.bonusOptions?.toSet())
        }
    }

    @Test
    fun racialBonusChoicesAreNotConfusedWithAutomaticLanguages() {
        val elf = LanguageRules.forRace("Elfo")!!
        assertTrue("Silvestre" in elf.bonusOptions.orEmpty())
        assertTrue("Silvestre" !in elf.automatic)
    }

    @Test
    fun strongheartHalflingUsesCoreHalflingLanguageRules() {
        assertEquals(
            LanguageRules.forRace("Halfling"),
            LanguageRules.forRace("Halfling Robusto")
        )
    }

    @Test
    fun humanAndHalfElfHaveOpenEndedBonusLanguageChoices() {
        assertNull(LanguageRules.optionsFor("Humano"))
        assertNull(LanguageRules.optionsFor("Meio-Elfo"))
    }

    @Test
    fun intelligenceModifierDeterminesBonusLanguageCount() {
        assertEquals(0, LanguageRules.bonusLanguageCount(9))
        assertEquals(0, LanguageRules.bonusLanguageCount(10))
        assertEquals(1, LanguageRules.bonusLanguageCount(12))
        assertEquals(3, LanguageRules.bonusLanguageCount(16))
        assertEquals(5, LanguageRules.bonusLanguageCount(20))
    }

    @Test
    fun unknownRaceDoesNotInventLanguageRules() {
        assertNull(LanguageRules.forRace("Raça caseira"))
    }
}
