package com.example

import com.example.dnd35.model.CharacterClassEntry
import com.example.dnd35.model.CharacterCreatureBond
import com.example.dnd35.model.CreatureBondKind
import com.example.dnd35.model.CreatureBondRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreatureBondRulesTest {
    @Test
    fun wizardAndSorcererLevelsAccumulateForFamiliars() {
        val classes = listOf(CharacterClassEntry("Mago", 3), CharacterClassEntry("Feiticeiro", 2))
        assertEquals(5, CreatureBondRules.familiarMasterLevel(classes))
        assertTrue(CreatureBondRules.familiarOptions(classes).any { it.id == "coruja" })
    }

    @Test
    fun nonFamiliarClassesDoNotUnlockFamiliars() {
        val classes = listOf(CharacterClassEntry("Guerreiro", 20), CharacterClassEntry("Druida", 20))
        assertEquals(0, CreatureBondRules.familiarMasterLevel(classes))
        assertTrue(CreatureBondRules.familiarOptions(classes).isEmpty())
    }

    @Test
    fun druidAndHalfRangerLevelsAccumulateForCompanions() {
        val classes = listOf(CharacterClassEntry("Druida", 2), CharacterClassEntry("Ranger", 6))
        assertEquals(5, CreatureBondRules.animalCompanionEffectiveLevel(classes))
        val options = CreatureBondRules.animalCompanionOptions(classes)
        assertTrue(options.any { it.creature.id == "lobo" })
        val direWeasel = options.first { it.creature.id == "arminho-atroz" }
        assertEquals(3, direWeasel.effectiveLevelPenalty)
        assertEquals(2, direWeasel.companionEffectiveLevel)
        assertFalse(options.any { it.creature.id == "gorila-atroz" })
    }

    @Test
    fun rangerAloneUsesHalfLevelAndCannotChooseAdvancedCompanionTooEarly() {
        val ranger7 = listOf(CharacterClassEntry("Ranger", 7))
        assertEquals(3, CreatureBondRules.animalCompanionEffectiveLevel(ranger7))
        assertFalse(CreatureBondRules.animalCompanionOptions(ranger7).any { it.creature.id == "arminho-atroz" })
        val ranger8 = listOf(CharacterClassEntry("Ranger", 8))
        assertEquals(4, CreatureBondRules.animalCompanionEffectiveLevel(ranger8))
        assertTrue(CreatureBondRules.animalCompanionOptions(ranger8).any { it.creature.id == "arminho-atroz" })
    }

    @Test
    fun aquaticCompanionsRequireAquaticCampaignFlag() {
        val druid = listOf(CharacterClassEntry("Druida", 10))
        assertFalse(CreatureBondRules.animalCompanionOptions(druid).any { it.creature.id == "golfinho" })
        assertTrue(CreatureBondRules.animalCompanionOptions(druid, aquaticCampaign = true).any { it.creature.id == "golfinho" })
    }

    @Test
    fun bondValidationUsesCatalogEligibilityFlags() {
        assertTrue(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.FAMILIAR, creatureId = "coruja")))
        assertTrue(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.ANIMAL_COMPANION, creatureId = "lobo")))
        assertFalse(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.FAMILIAR, creatureId = "lobo")))
        assertFalse(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.ANIMAL_COMPANION, creatureId = "abolete")))
    }
}
