package com.voicebell.clock.presentation.screens.timer

import com.voicebell.clock.domain.model.Timer

/**
 * UI state for Timer screen
 */
data class TimerState(
    /**
     * All timers (running, paused, finished)
     */
    val timers: List<Timer> = emptyList(),

    /**
     * Currently active (running or paused) timer
     */
    val activeTimer: Timer? = null,

    /**
     * Input hours for new timer
     */
    val inputHours: Int = 0,

    /**
     * Input minutes for new timer
     */
    val inputMinutes: Int = 0,

    /**
     * Input seconds for new timer
     */
    val inputSeconds: Int = 0,

    /**
     * Label for new timer
     */
    val inputLabel: String = "",

    /**
     * Whether loading
     */
    val isLoading: Boolean = false,

    /**
     * Error message
     */
    val errorMessage: String? = null,

    /**
     * Whether vibration is enabled for new timers
     */
    val vibrateEnabled: Boolean = true
) {
    /**
     * Total input duration in milliseconds
     */
    val inputDurationMillis: Long
        get() = (inputHours * 3600000L) + (inputMinutes * 60000L) + (inputSeconds * 1000L)

    /**
     * Whether start button should be enabled
     */
    val canStart: Boolean
        get() = inputDurationMillis > 0

    /**
     * Whether there are recent timers to show
     */
    val hasRecentTimers: Boolean
        get() = timers.isNotEmpty()
}
