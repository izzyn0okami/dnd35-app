package com.example

import com.example.dnd35.model.CharacterClassEntry
import com.example.dnd35.model.SkillPointAllocation
import com.example.dnd35.model.SkillPointLevelRecord
import com.example.dnd35.model.calculateSkillPointHistoryTotal
import com.example.dnd35.model.estimateSkillPointsForLevel
import com.example.dnd35.model.estimateTotalSkillPoints
import com.example.dnd35.model.parseSkillPointAllocations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class SkillPointBudgetTest {

    @Test
    fun firstLevelMultipliesClassPointsAndIntelligenceBeforeHumanBonus() {
        // Guerreiro: (2 pontos de classe + 0 Int) x4, depois +4 humano.
        assertEquals(12, estimateSkillPointsForLevel("Guerreiro", 1, 0, "Humano"))
        // Mago: (2 pontos de classe + 3 Int) x4, sem bônus humano.
        assertEquals(20, estimateSkillPointsForLevel("Mago", 1, 3, "Elfo"))
    }

    @Test
    fun minimumOnePointIsAppliedBeforeFirstLevelMultiplier() {
        // Modificador negativo não reduz o ganho abaixo de 1 ponto por nível.
        assertEquals(4, estimateSkillPointsForLevel("Mago", 1, -3, "Elfo"))
        assertEquals(8, estimateSkillPointsForLevel("Mago", 1, -3, "Humano"))
    }

    @Test
    fun laterHumanLevelsReceiveOneRacialPoint() {
        // Ladino: 8 pontos de classe +2 Int +1 humano no nível de personagem 2.
        assertEquals(11, estimateSkillPointsForLevel("Ladino", 2, 2, "Humano"))
        assertEquals(10, estimateSkillPointsForLevel("Ladino", 2, 2, "Halfling"))
    }

    @Test
    fun invalidLevelAndUnknownClassDoNotProduceAnEstimate() {
        assertNull(estimateSkillPointsForLevel("Mago", 0, 2, "Humano"))
        assertNull(estimateSkillPointsForLevel("Classe inexistente", 1, 2, "Humano"))
    }

    @Test
    fun allocationParserRejectsRepeatedSkillNamesAndAcceptsWholePointSpending() {
        assertEquals(
            listOf(
                SkillPointAllocation("Saltar", 2f),
                SkillPointAllocation("Escalar", 1f)
            ),
            parseSkillPointAllocations("Saltar = 2\nEscalar = 1")
        )
        assertNull(parseSkillPointAllocations("Saltar = 2\nSaltar = 1"))
        assertEquals(
            listOf(SkillPointAllocation("Perícia personalizada", 2f)),
            parseSkillPointAllocations("Perícia personalizada = 2")
        )
        assertEquals(
            listOf(SkillPointAllocation("Ofícios (Armadilhas)", 3f)),
            parseSkillPointAllocations("Ofícios (Armadilhas) = 3")
        )
        assertNull(parseSkillPointAllocations("Escalar = -1"))
        assertNull(parseSkillPointAllocations("Escalar = 1.5"))
    }

    @Test
    fun historyTotalUsesOnlyCurrentCharacterLevels() {
        val history = listOf(
            SkillPointLevelRecord(
                characterLevel = 1,
                className = "Guerreiro",
                availablePoints = 12f,
                allocations = listOf(SkillPointAllocation("Escalar", 2f))
            ),
            SkillPointLevelRecord(
                characterLevel = 2,
                className = "Guerreiro",
                availablePoints = 3f,
                allocations = listOf(SkillPointAllocation("Saltar", 3f))
            ),
            SkillPointLevelRecord(
                characterLevel = 3,
                className = "Guerreiro",
                availablePoints = 3f,
                allocations = listOf(SkillPointAllocation("Ouvir", 3f))
            )
        )

        assertEquals(
            com.example.dnd35.model.SkillPointHistoryTotal(isComplete = true, pointsSpent = 5f),
            calculateSkillPointHistoryTotal(history, totalCharacterLevel = 2)
        )
        assertEquals(
            com.example.dnd35.model.SkillPointHistoryTotal(isComplete = false, pointsSpent = 2f),
            calculateSkillPointHistoryTotal(history.take(1), totalCharacterLevel = 2)
        )
        assertNotNull(calculateSkillPointHistoryTotal(history, totalCharacterLevel = 2))
    }

    @Test
    fun duplicateCurrentLevelRecordsMakeHistoryIncomplete() {
        val levelOne = SkillPointLevelRecord(
            characterLevel = 1,
            className = "Guerreiro",
            availablePoints = 12f,
            allocations = listOf(SkillPointAllocation("Escalar", 2f))
        )
        val history = listOf(
            levelOne,
            levelOne.copy(allocations = listOf(SkillPointAllocation("Saltar", 3f))),
            SkillPointLevelRecord(
                characterLevel = 2,
                className = "Guerreiro",
                availablePoints = 3f,
                allocations = listOf(SkillPointAllocation("Ouvir", 1f))
            )
        )

        val result = calculateSkillPointHistoryTotal(history, totalCharacterLevel = 2)

        assertEquals(false, result.isComplete)
    }

    @Test
    fun totalEstimateSumsSingleClassLevelsWithFirstLevelMultiplierOnlyOnce() {
        // Guerreiro Humano nível 3, Int 0: 1º nível (2+0)x4+4 humano = 12; níveis 2 e 3 normais (2+1 cada) = 3+3.
        assertEquals(
            18,
            estimateTotalSkillPoints(
                listOf(CharacterClassEntry("Guerreiro", 3)),
                intelligenceModifier = 0,
                raceName = "Humano"
            )
        )
    }

    @Test
    fun totalEstimateAppliesMultiplierOnlyToTheFirstClassEnteredNotEveryClasssFirstLevel() {
        // Guerreiro 1 (classe inicial) + Ladino 2 (multiclasse), Humano, Int +1:
        // nível 1 (Guerreiro) multiplicado: (2+1)x4+4 = 16; níveis 2 e 3 (Ladino, normais): (8+1)+1 cada = 10+10.
        assertEquals(
            36,
            estimateTotalSkillPoints(
                listOf(CharacterClassEntry("Guerreiro", 1), CharacterClassEntry("Ladino", 2)),
                intelligenceModifier = 1,
                raceName = "Humano"
            )
        )
    }

    @Test
    fun totalEstimateKeepsFirstEntryAsTheOnlyInitialClassForMultiplier() {
        // A primeira entrada representa a classe inicial; a classe adicionada depois não recebe x4.
        val initialWarriorThenRogue = estimateTotalSkillPoints(
            listOf(CharacterClassEntry("Guerreiro", 1), CharacterClassEntry("Ladino", 1)),
            intelligenceModifier = 0,
            raceName = "Elfo"
        )
        val initialRogueThenWarrior = estimateTotalSkillPoints(
            listOf(CharacterClassEntry("Ladino", 1), CharacterClassEntry("Guerreiro", 1)),
            intelligenceModifier = 0,
            raceName = "Elfo"
        )
        assertEquals(16, initialWarriorThenRogue)
        assertEquals(34, initialRogueThenWarrior)
    }

    @Test
    fun totalEstimateHasNoRacialBonusForNonHumans() {
        // Elfo Mago nível 2, Int 3: 1º nível (2+3)x4 = 20; 2º nível normal (2+3) = 5.
        assertEquals(
            25,
            estimateTotalSkillPoints(
                listOf(CharacterClassEntry("Mago", 2)),
                intelligenceModifier = 3,
                raceName = "Elfo"
            )
        )
    }

    @Test
    fun totalEstimateIsNullForEmptyOrUnknownClasses() {
        assertNull(estimateTotalSkillPoints(emptyList(), intelligenceModifier = 2, raceName = "Humano"))
        assertNull(
            estimateTotalSkillPoints(
                listOf(CharacterClassEntry("Classe inexistente", 1)),
                intelligenceModifier = 2,
                raceName = "Humano"
            )
        )
    }

}
