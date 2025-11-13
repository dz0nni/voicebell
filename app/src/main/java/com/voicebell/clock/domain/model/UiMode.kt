package com.voicebell.clock.domain.model

/**
 * UI mode for the application layout.
 * Users can switch between classic and main screen layouts.
 */
enum class UiMode(val displayName: String) {
    /**
     * Main screen layout with all features on single screen:
     * - Recent alarms at top
     * - Recent timers below
     * - Large voice command button at bottom
     * - Stopwatch card at top (toggleable)
     * - Expandable FAB for quick add alarm/timer
     */
    EXPERIMENTAL("Main Screen"),

    /**
     * Classic layout with tabs for Alarm, Clock, Timer, Stopwatch
     */
    CLASSIC("Classic");

    companion object {
        fun fromString(value: String): UiMode {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: CLASSIC
        }
    }
}
