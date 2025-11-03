package com.voicebell.clock.presentation.screens.alarm

/**
 * Sealed class representing one-time side effects for the Alarm screen.
 * These are consumed once by the UI and don't persist in state.
 */
sealed class AlarmEffect {
    /**
     * Navigate to alarm creation screen
     */
    data object NavigateToCreateAlarm : AlarmEffect()

    /**
     * Navigate to alarm edit screen
     */
    data class NavigateToEditAlarm(val alarmId: Long) : AlarmEffect()

    /**
     * Navigate to settings screen
     */
    data object NavigateToSettings : AlarmEffect()

    /**
     * Show error message in snackbar
     */
    data class ShowError(val message: String) : AlarmEffect()

    /**
     * Show success message in snackbar
     */
    data class ShowSuccess(val message: String) : AlarmEffect()

    /**
     * Show alarm deleted message with undo option
     */
    data class ShowAlarmDeleted(val alarmId: Long, val alarm: com.voicebell.clock.domain.model.Alarm) : AlarmEffect()

    /**
     * Navigate to exact alarm permission settings
     */
    data object NavigateToExactAlarmSettings : AlarmEffect()
}
