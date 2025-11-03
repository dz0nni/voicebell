package com.voicebell.clock.presentation.screens.alarm

import com.voicebell.clock.domain.model.Alarm

/**
 * UI state for the Alarm screen.
 * Represents the complete state of the alarm list at any given moment.
 */
data class AlarmState(
    /**
     * List of alarms to display
     */
    val alarms: List<Alarm> = emptyList(),

    /**
     * Whether the screen is currently loading data
     */
    val isLoading: Boolean = false,

    /**
     * Error message to display, null if no error
     */
    val errorMessage: String? = null,

    /**
     * Alarm ID pending deletion (for confirmation dialog)
     */
    val alarmPendingDelete: Long? = null,

    /**
     * Whether delete confirmation dialog is visible
     */
    val showDeleteDialog: Boolean = false,

    /**
     * Next scheduled alarm for display at top of screen
     */
    val nextAlarm: Alarm? = null,

    /**
     * Whether user has permission to schedule exact alarms
     */
    val hasExactAlarmPermission: Boolean = true,

    /**
     * Whether empty state should be shown
     */
    val showEmptyState: Boolean = false
) {
    /**
     * Computed property for whether content should be shown
     */
    val showContent: Boolean
        get() = !isLoading && errorMessage == null && alarms.isNotEmpty()
}
