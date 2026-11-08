package com.example.dnd35.diagnostics

internal const val SLOW_FRAME_THRESHOLD_NS = 32_000_000L

internal fun isSlowFrameDuration(durationNanos: Long): Boolean =
    durationNanos >= SLOW_FRAME_THRESHOLD_NS
