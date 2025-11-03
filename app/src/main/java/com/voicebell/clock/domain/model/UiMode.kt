package com.voicebell.clock.domain.model

/**
 * UI mode for the application layout.
 * Users can switch between classic and experimental layouts.
 */
enum class UiMode(val displayName: String) {
    /**
     * Classic layout with tabs for Alarm, Clock, Timer, Stopwatch
     */
    CLASSIC("Classic"),

    /**
     * Experimental layout with all features on single screen:
     * - Recent alarms at top
     * - Recent timers below
     * - Large voice command button in center
     * - Stopwatch quick launch at bottom
     * - Expandable FAB for quick add alarm/timer
     */
    EXPERIMENTAL("Experimental");

    companion object {
        fun fromString(value: String): UiMode {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: CLASSIC
        }
    }
}
