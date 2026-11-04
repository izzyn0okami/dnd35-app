package com.example

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.SpellcastingRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric: CharacterCalculator.compute() passa por CharacterJsonAdapters, que loga avisos
// via android.util.Log quando o JSON é inválido; sem Robolectric, esse método não é
// mockado e qualquer aviso (mesmo de outro código já existente) derruba o teste.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SpellcastingRulesTest {

    @Test
    fun bonusSpellsRequireAbilityModAtLeastEqualToSpellLevel() {
        assertEquals(0, SpellcastingRules.bonusSpellsForAbilityMod(1, 0))
        assertEquals(0, SpellcastingRules.bonusSpellsForAbilityMod(3, 2))
        // Círculo 0 (truques/orações) nunca recebe bônus, mesmo com modificador alto.
        assertEquals(0, SpellcastingRules.bonusSpellsForAbilityMod(0, 10))
    }

    @Test
    fun bonusSpellsGrantOneSlotOncePassingTheThresholdAndAnotherEveryNinePointsAbove() {
        assertEquals(1, SpellcastingRules.bonusSpellsForAbilityMod(1, 1))
        assertEquals(1, SpellcastingRules.bonusSpellsForAbilityMod(1, 9))
        assertEquals(2, SpellcastingRules.bonusSpellsForAbilityMod(1, 10))
        assertEquals(1, SpellcastingRules.bonusSpellsForAbilityMod(3, 3))
        assertEquals(1, SpellcastingRules.bonusSpellsForAbilityMod(3, 11))
        assertEquals(2, SpellcastingRules.bonusSpellsForAbilityMod(3, 12))
    }

    @Test
    fun wizardSpellsPerDayAtLevelOneIncludesAbilityBonusOnlyOnReachableCircles() {
        // Mago nível 1, Int mod +3 (score 16-17): 0º círculo 3 (sem bônus), 1º círculo base 1 + bônus 1 = 2.
        val result = SpellcastingRules.spellsPerDayForClassLevel("Mago", 1, abilityMod = 3, hasDomainSlot = false)
        assertEquals(3, result?.get(0))
        assertEquals(2, result?.get(1))
        assertEquals(0, result?.get(2)) // 2º círculo inacessível no nível 1: sem bônus.
    }

    @Test
    fun clericDomainSlotAddsOneExtraSpellPerAccessibleCircleAboveZero() {
        val withDomain = SpellcastingRules.spellsPerDayForClassLevel("Clérigo", 1, abilityMod = 0, hasDomainSlot = true)
        val withoutDomain = SpellcastingRules.spellsPerDayForClassLevel("Clérigo", 1, abilityMod = 0, hasDomainSlot = false)
        assertEquals(withoutDomain?.get(0), withDomain?.get(0)) // círculo 0 não ganha espaço de domínio
        assertEquals((withoutDomain?.get(1) ?: 0) + 1, withDomain?.get(1))
    }

    @Test
    fun sorcererAndBardKnownSpellsAreExposedButPreparedCastersReturnNull() {
        assertTrue(SpellcastingRules.knownSpellsForClassLevel("Feiticeiro", 1) != null)
        assertTrue(SpellcastingRules.knownSpellsForClassLevel("Bardo", 2) != null)
        assertNull(SpellcastingRules.knownSpellsForClassLevel("Mago", 5))
        assertNull(SpellcastingRules.knownSpellsForClassLevel("Clérigo", 5))
    }

    @Test
    fun characterCalculatorKeepsSlotsSeparatePerClassInMulticlass() {
        // Mago 5 / Clérigo 1 (Int e Sab = 10, mod 0): cada classe tem seus próprios espaços de
        // 1º círculo; eles NÃO são somados numa lista única.
        val character = CharacterEntity(
            multiclassJson = "[{\"className\":\"Mago\",\"level\":5},{\"className\":\"Clérigo\",\"level\":1}]",
            baseInt = 10,
            baseWis = 10
        )
        val stats = CharacterCalculator.compute(character)
        val wizardSlots1 = stats.casterClasses.find { it.className == "Mago" }?.slotsByLevel?.get(1) ?: 0
        val clericSlots1 = stats.casterClasses.find { it.className == "Clérigo" }?.slotsByLevel?.get(1) ?: 0
        val wizardEntry = stats.spellSlots.single { it.level == 1 && it.className == "Mago" }
        val clericEntry = stats.spellSlots.single { it.level == 1 && it.className == "Clérigo" }
        assertEquals(wizardSlots1, wizardEntry.totalSlots)
        assertEquals(clericSlots1, clericEntry.totalSlots)
        assertTrue(wizardEntry.totalSlots > 0 && clericEntry.totalSlots > 0)
    }

    @Test
    fun usedSlotsArePerClassAndDoNotLeakBetweenMulticlassCasters() {
        val character = CharacterEntity(
            multiclassJson = "[{\"className\":\"Mago\",\"level\":5},{\"className\":\"Clérigo\",\"level\":1}]",
            baseInt = 10,
            baseWis = 10,
            spellSlotsJson = "[{\"level\":1,\"totalSlots\":0,\"usedSlots\":1,\"className\":\"Clérigo\"}]"
        )
        val stats = CharacterCalculator.compute(character)
        assertEquals(1, stats.spellSlots.single { it.level == 1 && it.className == "Clérigo" }.usedSlots)
        assertEquals(0, stats.spellSlots.single { it.level == 1 && it.className == "Mago" }.usedSlots)
    }

    @Test
    fun legacyUsedSlotsWithoutClassGoToTheFirstClassWithSlotsAtThatCircle() {
        // Formato antigo (uso somado, sem className): vai para a primeira classe com espaço no círculo.
        val character = CharacterEntity(
            characterClass = "Mago",
            level = 1,
            baseInt = 10,
            spellSlotsJson = "[{\"level\":1,\"totalSlots\":0,\"usedSlots\":1}]"
        )
        val stats = CharacterCalculator.compute(character)
        val slot1 = stats.spellSlots.single { it.level == 1 && it.className == "Mago" }
        assertEquals(1, slot1.usedSlots)
        assertTrue(slot1.totalSlots >= 1)
    }

    @Test
    fun slotClassNameRoundTripsThroughJsonAndOldFormatStillLoads() {
        val json = com.example.dnd35.model.CharacterJsonAdapters.serializeSpellSlots(
            listOf(com.example.dnd35.model.SpellSlotInfo(level = 2, totalSlots = 3, usedSlots = 1, className = "Clérigo"))
        )
        val back = com.example.dnd35.model.CharacterJsonAdapters.deserializeSpellSlots(json).single()
        assertEquals("Clérigo", back.className)
        val legacy = com.example.dnd35.model.CharacterJsonAdapters
            .deserializeSpellSlots("[{\"level\":2,\"totalSlots\":0,\"usedSlots\":1}]").single()
        assertEquals("", legacy.className)
        assertEquals(1, legacy.usedSlots)
    }
}
