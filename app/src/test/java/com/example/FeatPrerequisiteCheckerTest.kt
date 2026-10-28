package com.example

import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.FeatPrerequisiteChecker
import com.example.dnd35.model.FeatPrerequisiteStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FeatPrerequisiteCheckerTest {
    @Test
    fun checksAbilityAndBaseAttackBonusWithoutBlockingAnything() {
        val stats = CharacterCalculator.compute(
            CharacterEntity(characterClass = "Guerreiro", level = 1, baseStr = 14, baseDex = 12)
        )

        assertEquals(
            FeatPrerequisiteStatus.MET,
            FeatPrerequisiteChecker.assess("FOR 13, BBA +1", stats).status
        )
        val missing = FeatPrerequisiteChecker.assess("DES 15", stats)
        assertEquals(FeatPrerequisiteStatus.NOT_MET, missing.status)
        assertTrue(missing.message.contains("DES 15"))
    }

    @Test
    fun checksCharacterAndClassLevels() {
        val levelOne = CharacterCalculator.compute(CharacterEntity(characterClass = "Guerreiro", level = 1))
        val levelFour = CharacterCalculator.compute(CharacterEntity(characterClass = "Guerreiro", level = 4))

        assertEquals(
            FeatPrerequisiteStatus.NOT_MET,
            FeatPrerequisiteChecker.assess("Guerreiro 4º nível", levelOne).status
        )
        assertEquals(
            FeatPrerequisiteStatus.MET,
            FeatPrerequisiteChecker.assess("Guerreiro 4º nível", levelFour).status
        )
        assertEquals(
            FeatPrerequisiteStatus.MET,
            FeatPrerequisiteChecker.assess("4º nível de personagem", levelFour).status
        )
    }

    @Test
    fun reportsRequirementsThatNeedUntrackedChoicesAsPartiallyVerified() {
        val stats = CharacterCalculator.compute(CharacterEntity(characterClass = "Guerreiro", level = 1))
        val result = FeatPrerequisiteChecker.assess("Proficiência com a arma escolhida", stats)

        assertEquals(FeatPrerequisiteStatus.PARTIAL, result.status)
        assertTrue(result.message.contains("Não verificados"))
    }

    @Test
    fun recognizesSelectedVariantOfPrerequisiteFeat() {
        val stats = CharacterCalculator.compute(
            CharacterEntity(
                characterClass = "Guerreiro",
                level = 4,
                featsJson = """[{"name":"Foco em Arma (Espada Longa)"}]"""
            )
        )

        val result = FeatPrerequisiteChecker.assess(
            "Foco em Arma com a arma escolhida",
            stats
        )

        // A ficha registra o talento escolhido, mas não guarda a arma como campo próprio;
        // portanto, não deve acusar o talento como ausente nem afirmar a escolha verificada.
        assertTrue(result.unmet.isEmpty())
        assertEquals(FeatPrerequisiteStatus.PARTIAL, result.status)
        assertTrue(result.message.contains("Não verificados"))
    }

    @Test
    fun noPrerequisiteIsAutomaticallySatisfied() {
        val stats = CharacterCalculator.compute(CharacterEntity())
        assertEquals(
            FeatPrerequisiteStatus.MET,
            FeatPrerequisiteChecker.assess("Nenhum", stats).status
        )
    }
}
