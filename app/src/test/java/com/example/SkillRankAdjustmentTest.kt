package com.example

import com.example.dnd35.viewmodel.skillRankAdjustmentStep
import org.junit.Assert.assertEquals
import org.junit.Test

class SkillRankAdjustmentTest {
    @Test
    fun classSkillWithFractionalRanksUsesHalfRankStep() {
        assertEquals(0.5f, skillRankAdjustmentStep(0.5f, true))
        assertEquals(0.5f, skillRankAdjustmentStep(1.5f, true))
    }

    @Test
    fun classSkillWithWholeRanksUsesFullRankStep() {
        assertEquals(1.0f, skillRankAdjustmentStep(0f, true))
        assertEquals(1.0f, skillRankAdjustmentStep(2f, true))
    }

    @Test
    fun crossClassSkillAlwaysUsesHalfRankStep() {
        assertEquals(0.5f, skillRankAdjustmentStep(0f, false))
        assertEquals(0.5f, skillRankAdjustmentStep(1.5f, false))
    }
}
