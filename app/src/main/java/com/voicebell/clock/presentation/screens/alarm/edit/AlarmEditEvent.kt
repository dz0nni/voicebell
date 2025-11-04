package com.voicebell.clock.presentation.screens.alarm.edit

import com.voicebell.clock.domain.model.DayOfWeek

/**
 * User events for alarm edit/create screen
 */
sealed class AlarmEditEvent {
    /**
     * User changed hour
     */
    data class SetHour(val hour: Int) : AlarmEditEvent()

    /**
     * User changed minute
     */
    data class SetMinute(val minute: Int) : AlarmEditEvent()

    /**
     * User changed label
     */
    data class SetLabel(val label: String) : AlarmEditEvent()

    /**
     * User toggled a repeat day
     */
    data class ToggleRepeatDay(val day: DayOfWeek) : AlarmEditEvent()

    /**
     * User selected alarm tone
     */
    data class SetAlarmTone(val tone: String) : AlarmEditEvent()

    /**
     * User toggled vibration
     */
    data class ToggleVibrate(val enabled: Boolean) : AlarmEditEvent()

    /**
     * User toggled flash
     */
    data class ToggleFlash(val enabled: Boolean) : AlarmEditEvent()

    /**
     * User toggled gradual volume
     */
    data class ToggleGradualVolume(val enabled: Boolean) : AlarmEditEvent()

    /**
     * User changed volume level
     */
    data class SetVolumeLevel(val level: Int) : AlarmEditEvent()

    /**
     * User toggled snooze enabled
     */
    data class ToggleSnooze(val enabled: Boolean) : AlarmEditEvent()

    /**
     * User changed snooze duration
     */
    data class SetSnoozeDuration(val minutes: Int) : AlarmEditEvent()

    /**
     * User changed max snooze count
     */
    data class SetMaxSnoozeCount(val count: Int) : AlarmEditEvent()

    /**
     * User changed pre-alarm count
     */
    data class SetPreAlarmCount(val count: Int) : AlarmEditEvent()

    /**
     * User changed pre-alarm interval
     */
    data class SetPreAlarmInterval(val minutes: Int) : AlarmEditEvent()

    /**
     * User clicked save button
     */
    data object SaveAlarm : AlarmEditEvent()

    /**
     * User clicked cancel button
     */
    data object Cancel : AlarmEditEvent()

    /**
     * User clicked delete button (edit mode only)
     */
    data object DeleteAlarm : AlarmEditEvent()
}
