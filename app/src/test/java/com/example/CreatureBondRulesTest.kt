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
    fun rangerUsesItsRestrictedBasicListAndAquaticOptionsAreConditional() {
        val ranger4 = listOf(CharacterClassEntry("Ranger", 4))
        val ordinary = CreatureBondRules.animalCompanionOptions(ranger4)
        assertTrue(ordinary.any { it.creature.id == "lobo" })
        assertFalse(ordinary.any { it.creature.id == "camelo" })
        assertFalse(ordinary.any { it.creature.id == "crocodilo" })

        val aquatic = CreatureBondRules.animalCompanionOptions(ranger4, aquaticCampaign = true)
        assertTrue(aquatic.any { it.creature.id == "crocodilo" })
        assertTrue(aquatic.any { it.creature.id == "golfinho" })
    }

    @Test
    fun aquaticCompanionsRequireAquaticCampaignFlag() {
        val druid = listOf(CharacterClassEntry("Druida", 10))
        assertFalse(CreatureBondRules.animalCompanionOptions(druid).any { it.creature.id == "golfinho" })
        assertTrue(CreatureBondRules.animalCompanionOptions(druid, aquaticCampaign = true).any { it.creature.id == "golfinho" })
        assertTrue(CreatureBondRules.animalCompanionOptions(druid, aquaticCampaign = true).any { it.creature.id == "elasmossauro" })
    }

    @Test
    fun alternativeCompanionListsContinueAtLevelsFourTenThirteenAndSixteen() {
        val druid4 = listOf(CharacterClassEntry("Druida", 4))
        val options4 = CreatureBondRules.animalCompanionOptions(druid4)
        assertTrue(options4.any { it.creature.id == "texugo-atroz" && it.effectiveLevelPenalty == 3 })

        val druid10 = listOf(CharacterClassEntry("Druida", 10))
        val options10 = CreatureBondRules.animalCompanionOptions(druid10, aquaticCampaign = true)
        assertTrue(options10.any { it.creature.id == "megaraptor" && it.effectiveLevelPenalty == 9 })
        assertTrue(options10.any { it.creature.id == "cobra-constritora-gigante" && it.effectiveLevelPenalty == 9 })
        assertTrue(options10.any { it.creature.id == "leao-atroz" && it.effectiveLevelPenalty == 9 })

        val druid13 = listOf(CharacterClassEntry("Druida", 13))
        val options13 = CreatureBondRules.animalCompanionOptions(druid13, aquaticCampaign = true)
        assertTrue(options13.any { it.creature.id == "elefante" && it.effectiveLevelPenalty == 12 })
        assertTrue(options13.any { it.creature.id == "polvo-gigante" && it.effectiveLevelPenalty == 12 })

        val druid16 = listOf(CharacterClassEntry("Druida", 16))
        val options16 = CreatureBondRules.animalCompanionOptions(druid16, aquaticCampaign = true)
        assertTrue(options16.any { it.creature.id == "tiranossauro" && it.effectiveLevelPenalty == 15 })
        assertTrue(options16.any { it.creature.id == "tubarao-atroz" && it.effectiveLevelPenalty == 15 })
    }

    @Test
    fun familiarProgressionUsesCombinedWizardAndSorcererLevelsAtEveryTier() {
        val cases = listOf(
            1 to (1 to 6),
            2 to (1 to 6),
            3 to (2 to 7),
            5 to (3 to 8),
            7 to (4 to 9),
            9 to (5 to 10),
            11 to (6 to 11),
            13 to (7 to 12),
            15 to (8 to 13),
            17 to (9 to 14),
            19 to (10 to 15),
            20 to (10 to 15)
        )
        cases.forEach { (level, expected) ->
            val progression = CreatureBondRules.familiarProgression(
                listOf(CharacterClassEntry("Mago", level / 2), CharacterClassEntry("Feiticeiro", level - level / 2)),
                "coruja"
            )!!
            assertEquals(level, progression.masterLevel)
            assertEquals(expected.first, progression.naturalArmorBonus)
            assertEquals(expected.second, progression.intelligence)
        }
    }

    @Test
    fun familiarSpecialAbilitiesUnlockAtCorrectLevelsAndResistanceBelongsToFamiliar() {
        fun progress(level: Int) = CreatureBondRules.familiarProgression(
            listOf(CharacterClassEntry("Feiticeiro", level)), "gato"
        )!!

        assertFalse(progress(2).familiarFeatures.any { it.contains("Transmitir magias") })
        assertTrue(progress(3).familiarFeatures.any { it.contains("Transmitir magias de toque") })
        assertTrue(progress(5).familiarFeatures.any { it.contains("Falar com o mestre") })
        assertTrue(progress(7).familiarFeatures.any { it.contains("animais de sua espécie") })
        assertEquals(null, progress(10).spellResistance)
        assertEquals(16, progress(11).spellResistance)
        assertEquals(0, progress(12).scryingUsesPerDay)
        assertEquals(1, progress(13).scryingUsesPerDay)
        assertTrue(progress(13).masterBenefits.any { it.title == "Vidência no familiar" })
    }

    @Test
    fun familiarSpeciesBenefitsAreGrantedToMasterWithCorrectRangeConditions() {
        val owl = CreatureBondRules.familiarProgression(
            listOf(CharacterClassEntry("Mago", 1)), "coruja"
        )!!
        assertTrue(owl.masterBenefits.any {
            it.title == "Prontidão" &&
                it.effect == "+2 em Observar e Ouvir" &&
                it.condition.contains("ao alcance da mão")
        })
        assertTrue(owl.masterBenefits.any {
            it.title == "Coruja" &&
                it.effect == "+3 em Observar na penumbra" &&
                it.condition.contains("1,5 km")
        })

        val toad = CreatureBondRules.familiarProgression(
            listOf(CharacterClassEntry("Feiticeiro", 1)), "sapo"
        )!!
        assertTrue(toad.masterBenefits.any { it.effect == "+3 pontos de vida" })

        val raven = CreatureBondRules.familiarProgression(
            listOf(CharacterClassEntry("Mago", 7)), "corvo"
        )!!
        assertTrue(raven.familiarFeatures.any { it.contains("Pode falar um idioma") })
        assertTrue(raven.masterBenefits.any { it.effect == "+3 em testes de Avaliação" })
    }

    @Test
    fun familiarProgressionIsUnavailableWithoutFamiliarClassOrEligibleCreature() {
        assertEquals(
            null,
            CreatureBondRules.familiarProgression(listOf(CharacterClassEntry("Guerreiro", 20)), "coruja")
        )
        assertEquals(
            null,
            CreatureBondRules.familiarProgression(listOf(CharacterClassEntry("Mago", 5)), "lobo")
        )
    }

    @Test
    fun bondValidationUsesCatalogEligibilityFlags() {
        assertTrue(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.FAMILIAR, creatureId = "coruja")))
        assertTrue(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.ANIMAL_COMPANION, creatureId = "lobo")))
        assertFalse(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.FAMILIAR, creatureId = "lobo")))
        assertFalse(CreatureBondRules.isValidBond(CharacterCreatureBond(kind = CreatureBondKind.ANIMAL_COMPANION, creatureId = "abolete")))
    }
}
