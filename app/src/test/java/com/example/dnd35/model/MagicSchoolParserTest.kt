package com.example.dnd35.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MagicSchoolParserTest {
    @Test
    fun matchesBaseSchoolRegardlessOfSubschoolAndDescriptors() {
        assertTrue(MagicSchoolParser.matchesBaseSchool("Conjuração (Invocação)", "Conjuração"))
        assertTrue(MagicSchoolParser.matchesBaseSchool("Conjuração (Criação) [Fogo]", "Conjuração"))
        assertTrue(MagicSchoolParser.matchesBaseSchool("Encantamento (Compulsão) [Ação Mental]", "Encantamento"))
        assertTrue(MagicSchoolParser.matchesBaseSchool("Transmutação [Água]", "Transmutação"))
        assertFalse(MagicSchoolParser.matchesBaseSchool("Transmutação [Água]", "Conjuração"))
    }

    @Test
    fun parsesSubschoolDescriptorsAndNonDescriptorNotesSeparately() {
        val parsed = MagicSchoolParser.parse(
            "Conjuração (Invocação) [veja texto de Invocar Criaturas I]"
        )

        assertEquals("Conjuração", parsed.baseSchool)
        assertEquals("Invocação", parsed.subschool)
        assertTrue(parsed.descriptors.isEmpty())
        assertEquals(listOf("veja texto de Invocar Criaturas I"), parsed.annotations)
    }

    @Test
    fun recognizesDescriptorsWithoutBreakingLegacySchoolText() {
        val raw = "Encantamento (Compulsão) [Ação Mental]"
        val parsed = MagicSchoolParser.parse(raw)

        assertEquals("Encantamento", parsed.baseSchool)
        assertEquals("Compulsão", parsed.subschool)
        assertEquals(listOf("Ação Mental"), parsed.descriptors)
    }

    @Test
    fun everyCatalogSpellHasOneOfTheEightBasicSchoolsOrUniversal() {
        val accepted = setOf(
            "Abjuração", "Adivinhação", "Conjuração", "Encantamento",
            "Evocação", "Ilusão", "Necromancia", "Transmutação", "Universal"
        )

        assertTrue(Dnd35Rules.PRESET_SPELLS.isNotEmpty())
        Dnd35Rules.PRESET_SPELLS.forEach { spell ->
            assertTrue(
                "${spell.name}: escola base inesperada em '${spell.school}'",
                MagicSchoolParser.parse(spell.school).baseSchool in accepted
            )
        }
    }

    @Test
    fun descriptorRecognitionFollowsTheBrazilianPlayerHandbookList() {
        val mentalAction = MagicSchoolParser.parse("Encantamento (Compulsão) [Ação Mental]")
        val languageDependent = MagicSchoolParser.parse("Encantamento (Compulsão) [Dependente de idioma]")
        val unlisted = MagicSchoolParser.parse("Evocação [Força]")

        assertEquals(listOf("Ação Mental"), mentalAction.descriptors)
        assertEquals(listOf("Dependente de idioma"), languageDependent.descriptors)
        // "Força" não aparece na lista de descritores do trecho consultado; preservá-lo
        // como anotação evita classificá-lo silenciosamente como descritor oficial.
        assertTrue(unlisted.descriptors.isEmpty())
        assertEquals(listOf("Força"), unlisted.annotations)
    }

    @Test
    fun universalIsASeparateFilterAndAllMatchesEverySchool() {
        assertTrue(MagicSchoolParser.matchesBaseSchool("Universal", "Universal"))
        assertFalse(MagicSchoolParser.matchesBaseSchool("Necromancia [Morte]", "Universal"))
        assertTrue(MagicSchoolParser.matchesBaseSchool("Necromancia [Morte]", "Todas"))
    }
}
