package com.example

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellbookRules
import com.example.dnd35.model.SpellcastingRules
import com.example.dnd35.model.WizardSpecialty
import com.example.dnd35.model.WizardSpecialtyRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WizardSpecialtyRulesTest {

    @Test
    fun normalizeLimitsProhibitedSchoolsAndProtectsDivination() {
        val s = WizardSpecialtyRules.normalize("evocação", listOf("Necromancia", "Encantamento", "Ilusão", "Evocação"))
        assertEquals(WizardSpecialty("Evocação", listOf("Necromancia", "Encantamento")), s)
        // Adivinhação nunca pode ser proibida; especialista em Adivinhação proíbe só 1.
        assertEquals(WizardSpecialty("Adivinhação", listOf("Necromancia")), WizardSpecialtyRules.normalize("Adivinhação", listOf("Necromancia", "Ilusão")))
        assertEquals(listOf("Ilusão"), WizardSpecialtyRules.normalize("Evocação", listOf("Adivinhação", "Ilusão")).prohibited)
        // Generalista (ou escola inválida) não tem proibições.
        assertEquals(WizardSpecialty(), WizardSpecialtyRules.normalize(null, listOf("Ilusão")))
        assertEquals(WizardSpecialty(), WizardSpecialtyRules.normalize("Inexistente", listOf("Ilusão")))
    }

    @Test
    fun specialtyRoundTripsThroughJson() {
        val original = WizardSpecialty("Evocação", listOf("Necromancia", "Encantamento"))
        assertEquals(original, CharacterJsonAdapters.deserializeSpecialty(CharacterJsonAdapters.serializeSpecialty(original)))
        assertEquals("", CharacterJsonAdapters.serializeSpecialty(WizardSpecialty()))
        assertEquals(WizardSpecialty(), CharacterJsonAdapters.deserializeSpecialty(""))
        assertEquals(WizardSpecialty(), CharacterJsonAdapters.deserializeSpecialty("lixo"))
    }

    @Test
    fun specialistGetsOneExtraSlotPerCircleIncludingZero() {
        val base = SpellcastingRules.spellsPerDayForClassLevel("Mago", 5, 0, hasDomainSlot = false)!!
        val specialist = SpellcastingRules.spellsPerDayForClassLevel("Mago", 5, 0, hasDomainSlot = false, hasSpecialistSlot = true)!!
        (0..3).forEach { assertEquals("círculo $it", base[it] + 1, specialist[it]) }
        // Círculos ainda inacessíveis não ganham espaço.
        (4..9).forEach { assertEquals("círculo $it", 0, specialist[it]) }
    }

    @Test
    fun characterCalculatorAppliesSpecialistSlotOnlyToWizards() {
        val json = CharacterJsonAdapters.serializeSpecialty(WizardSpecialty("Evocação", listOf("Necromancia", "Encantamento")))
        val generalist = CharacterCalculator.compute(CharacterEntity(characterClass = "Mago", level = 5, baseInt = 10))
        val specialist = CharacterCalculator.compute(CharacterEntity(characterClass = "Mago", level = 5, baseInt = 10, wizardSpecialtyJson = json))
        val g = generalist.spellSlots.single { it.level == 1 }.totalSlots
        val s = specialist.spellSlots.single { it.level == 1 }.totalSlots
        assertEquals(g + 1, s)
        val cleric = CharacterCalculator.compute(CharacterEntity(characterClass = "Clérigo", level = 5, baseWis = 10, wizardSpecialtyJson = json))
        val clericBase = CharacterCalculator.compute(CharacterEntity(characterClass = "Clérigo", level = 5, baseWis = 10))
        assertEquals(clericBase.spellSlots.single { it.level == 1 }.totalSlots, cleric.spellSlots.single { it.level == 1 }.totalSlots)
    }

    @Test
    fun prohibitedSchoolBlocksLearningForWizardsOnly() {
        val stats = CharacterCalculator.compute(CharacterEntity(characterClass = "Mago", level = 5, baseInt = 16))
        val wizard = stats.casterClasses.single()
        val fireball = Dnd35Rules.PRESET_SPELLS.single { it.name == "Bola de Fogo" }
        val necro = Dnd35Rules.PRESET_SPELLS.first { it.classLevels.containsKey("Mago") && it.school.startsWith("Necromancia") && it.level <= 3 }
        val banned = WizardSpecialty("Evocação", listOf("Necromancia", "Encantamento"))
        assertEquals(SpellbookRules.Eligibility.PROHIBITED_SCHOOL, SpellbookRules.eligibility(necro, wizard, 16, banned))
        assertEquals(SpellbookRules.Eligibility.OK, SpellbookRules.eligibility(fireball, wizard, 16, banned))
        assertEquals(SpellbookRules.Eligibility.OK, SpellbookRules.eligibility(necro, wizard, 16))
    }

    @Test
    fun warningsFlagProhibitedAndCommonSpellsInTheSpecialtySlot() {
        val stats = CharacterCalculator.compute(
            CharacterEntity(
                characterClass = "Mago", level = 1, baseInt = 10,
                wizardSpecialtyJson = CharacterJsonAdapters.serializeSpecialty(WizardSpecialty("Evocação", listOf("Necromancia", "Encantamento")))
            )
        )
        val wizard = stats.casterClasses.single()
        val specialty = WizardSpecialtyRules.specialtyOf(stats.character)
        val slots1 = wizard.slotsByLevel.getValue(1)
        // Mago 1: base 1 + 1 de especialista = 2 espaços de 1º círculo; 2 magias comuns invadem o espaço da especialidade.
        assertEquals(2, slots1)
        val common = listOf(
            CharacterSpell(name = "A", level = 1, school = "Abjuração", casterClass = "Mago", prepared = true),
            CharacterSpell(name = "B", level = 1, school = "Ilusão", casterClass = "Mago", prepared = true)
        )
        assertTrue(WizardSpecialtyRules.warnings(common, specialty, wizard).any { it.contains("especialidade") })
        val ok = listOf(common[0], CharacterSpell(name = "C", level = 1, school = "Evocação", casterClass = "Mago", prepared = true))
        assertTrue(WizardSpecialtyRules.warnings(ok, specialty, wizard).isEmpty())
        val prohibited = listOf(CharacterSpell(name = "D", level = 1, school = "Necromancia [Mal]", casterClass = "Mago", prepared = true))
        assertTrue(WizardSpecialtyRules.warnings(prohibited, specialty, wizard).any { it.contains("proibida") })
        assertFalse(WizardSpecialtyRules.warnings(prohibited, WizardSpecialty(), wizard).any { it.contains("proibida") })
    }
}
