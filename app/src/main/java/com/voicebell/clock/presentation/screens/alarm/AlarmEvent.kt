package com.voicebell.clock.presentation.screens.alarm

/**
 * Sealed class representing all user interactions with the Alarm screen.
 * Following MVI pattern for unidirectional data flow.
 */
sealed class AlarmEvent {
    /**
     * User toggled alarm on/off switch
     */
    data class ToggleAlarm(val alarmId: Long, val isEnabled: Boolean) : AlarmEvent()

    /**
     * User clicked on an alarm to edit it
     */
    data class EditAlarm(val alarmId: Long) : AlarmEvent()

    /**
     * User clicked delete on an alarm
     */
    data class DeleteAlarm(val alarmId: Long) : AlarmEvent()

    /**
     * User confirmed deletion in dialog
     */
    data class ConfirmDelete(val alarmId: Long) : AlarmEvent()

    /**
     * User cancelled deletion dialog
     */
    data object CancelDelete : AlarmEvent()

    /**
     * User clicked FAB to create new alarm
     */
    data object CreateNewAlarm : AlarmEvent()

    /**
     * User refreshed the alarm list
     */
    data object RefreshAlarms : AlarmEvent()

    /**
     * User dismissed an error message
     */
    data object DismissError : AlarmEvent()

    /**
     * User clicked on alarm settings
     */
    data object OpenSettings : AlarmEvent()
}
