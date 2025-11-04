package com.voicebell.clock.presentation.screens.timer

import com.voicebell.clock.domain.model.Timer

/**
 * User events for Timer screen
 */
sealed class TimerEvent {
    /**
     * User changed hours input
     */
    data class SetHours(val hours: Int) : TimerEvent()

    /**
     * User changed minutes input
     */
    data class SetMinutes(val minutes: Int) : TimerEvent()

    /**
     * User changed seconds input
     */
    data class SetSeconds(val seconds: Int) : TimerEvent()

    /**
     * User changed label
     */
    data class SetLabel(val label: String) : TimerEvent()

    /**
     * User toggled vibration
     */
    data class ToggleVibrate(val enabled: Boolean) : TimerEvent()

    /**
     * User clicked start button (new timer)
     */
    data object StartTimer : TimerEvent()

    /**
     * User clicked pause on active timer
     */
    data object PauseTimer : TimerEvent()

    /**
     * User clicked resume on paused timer
     */
    data object ResumeTimer : TimerEvent()

    /**
     * User clicked stop/cancel on active timer
     */
    data object StopTimer : TimerEvent()

    /**
     * User clicked restart on a previous timer
     */
    data class RestartTimer(val timer: Timer) : TimerEvent()

    /**
     * User clicked delete on a timer
     */
    data class DeleteTimer(val timerId: Long) : TimerEvent()

    /**
     * User dismissed error message
     */
    data object DismissError : TimerEvent()
}
