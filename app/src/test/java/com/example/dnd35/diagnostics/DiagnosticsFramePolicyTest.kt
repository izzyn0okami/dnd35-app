package com.example.dnd35.diagnostics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsFramePolicyTest {
    @Test
    fun frameBelowThresholdIsNotRecorded() {
        assertFalse(isSlowFrameDuration(SLOW_FRAME_THRESHOLD_NS - 1L))
    }

    @Test
    fun frameAtOrAboveThresholdIsRecorded() {
        assertTrue(isSlowFrameDuration(SLOW_FRAME_THRESHOLD_NS))
        assertTrue(isSlowFrameDuration(SLOW_FRAME_THRESHOLD_NS + 1L))
    }

    @Test
    fun zeroAndNegativeDurationsAreNotRecorded() {
        assertFalse(isSlowFrameDuration(0L))
        assertFalse(isSlowFrameDuration(-1L))
    }
}
