package com.example

import com.example.dnd35.model.CharacterClassEntry
import com.example.dnd35.model.calculateMulticlassXpPenalty
import org.junit.Assert.assertEquals
import org.junit.Test

class MulticlassXpPenaltyTest {
    @Test
    fun anyFavoredClassRaceNeverGetsMulticlassXpPenalty() {
        val classes = listOf(
            CharacterClassEntry("Guerreiro", 8),
            CharacterClassEntry("Mago", 5),
            CharacterClassEntry("Ladino", 2)
        )

        val result = calculateMulticlassXpPenalty(classes, "Qualquer")

        assertEquals(0, result.percentage)
        assertEquals(emptyList<CharacterClassEntry>(), result.offendingClasses)
    }

    @Test
    fun raceWithSpecificFavoredClassPenalizesLowerNonFavoredClasses() {
        val classes = listOf(
            CharacterClassEntry("Guerreiro", 8),
            CharacterClassEntry("Mago", 5),
            CharacterClassEntry("Ladino", 2)
        )

        val result = calculateMulticlassXpPenalty(classes, "Guerreiro")

        assertEquals(20, result.percentage)
        assertEquals(listOf(CharacterClassEntry("Ladino", 2)), result.offendingClasses)
    }

    @Test
    fun onlyOneNonFavoredClassDoesNotCausePenalty() {
        val classes = listOf(
            CharacterClassEntry("Guerreiro", 8),
            CharacterClassEntry("Mago", 2)
        )

        val result = calculateMulticlassXpPenalty(classes, "Guerreiro")

        assertEquals(0, result.percentage)
        assertEquals(emptyList<CharacterClassEntry>(), result.offendingClasses)
    }
}
