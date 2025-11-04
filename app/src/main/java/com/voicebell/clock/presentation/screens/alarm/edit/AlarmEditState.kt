package com.voicebell.clock.presentation.screens.alarm.edit

import com.voicebell.clock.domain.model.AlarmTone
import com.voicebell.clock.domain.model.DayOfWeek
import java.time.LocalTime

/**
 * UI state for alarm edit/create screen
 */
data class AlarmEditState(
    /**
     * Alarm ID (null for new alarm, non-null for editing)
     */
    val alarmId: Long? = null,

    /**
     * Whether we're in edit mode (true) or create mode (false)
     */
    val isEditMode: Boolean = false,

    /**
     * Selected hour (0-23)
     */
    val hour: Int = LocalTime.now().hour,

    /**
     * Selected minute (0-59)
     */
    val minute: Int = 0,

    /**
     * Alarm label/name
     */
    val label: String = "",

    /**
     * Days of week when alarm repeats
     */
    val repeatDays: Set<DayOfWeek> = emptySet(),

    /**
     * Selected alarm tone
     */
    val alarmTone: AlarmTone = AlarmTone.DEFAULT,

    /**
     * Whether vibration is enabled
     */
    val vibrate: Boolean = true,

    /**
     * Whether flash is enabled
     */
    val flash: Boolean = false,

    /**
     * Whether gradual volume increase is enabled
     */
    val gradualVolumeIncrease: Boolean = true,

    /**
     * Volume level (0-100)
     */
    val volumeLevel: Int = 80,

    /**
     * Whether snooze is enabled
     */
    val snoozeEnabled: Boolean = true,

    /**
     * Snooze duration in minutes
     */
    val snoozeDuration: Int = 10,

    /**
     * Maximum snooze count
     */
    val maxSnoozeCount: Int = 3,

    /**
     * Number of pre-alarms (0-10)
     */
    val preAlarmCount: Int = 0,

    /**
     * Interval between pre-alarms in minutes
     */
    val preAlarmInterval: Int = 7,

    /**
     * Whether the form is being saved
     */
    val isSaving: Boolean = false,

    /**
     * Error message to display
     */
    val errorMessage: String? = null,

    /**
     * Whether to show the delete confirmation dialog
     */
    val showDeleteDialog: Boolean = false
) {
    /**
     * Formatted time string for display
     */
    val formattedTime: String
        get() = String.format("%02d:%02d", hour, minute)

    /**
     * Whether the save button should be enabled
     */
    val canSave: Boolean
        get() = !isSaving && errorMessage == null

    /**
     * Screen title based on mode
     */
    val screenTitle: String
        get() = if (isEditMode) "Edit Alarm" else "New Alarm"
}
