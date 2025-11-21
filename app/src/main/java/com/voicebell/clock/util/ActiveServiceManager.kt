package com.voicebell.clock.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for tracking active alarms and timers.
 *
 * This allows the UI to know when alarms/timers are ringing
 * so they can be controlled from anywhere in the app.
 */
@Singleton
class ActiveServiceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "active_services",
        Context.MODE_PRIVATE
    )

    private val _activeAlarmId = MutableStateFlow<Long?>(null)
    val activeAlarmId: StateFlow<Long?> = _activeAlarmId.asStateFlow()

    private val _activeTimerId = MutableStateFlow<Long?>(null)
    val activeTimerId: StateFlow<Long?> = _activeTimerId.asStateFlow()

    init {
        // Load initial state
        val alarmId = prefs.getLong(KEY_ACTIVE_ALARM, -1L)
        _activeAlarmId.value = if (alarmId == -1L) null else alarmId

        val timerId = prefs.getLong(KEY_ACTIVE_TIMER, -1L)
        _activeTimerId.value = if (timerId == -1L) null else timerId
    }

    /**
     * Mark alarm as active (ringing).
     */
    fun setAlarmActive(alarmId: Long) {
        prefs.edit().putLong(KEY_ACTIVE_ALARM, alarmId).apply()
        _activeAlarmId.value = alarmId
    }

    /**
     * Clear active alarm.
     */
    fun clearActiveAlarm() {
        prefs.edit().remove(KEY_ACTIVE_ALARM).apply()
        _activeAlarmId.value = null
    }

    /**
     * Mark timer as active (ringing/finished).
     */
    fun setTimerActive(timerId: Long) {
        prefs.edit().putLong(KEY_ACTIVE_TIMER, timerId).apply()
        _activeTimerId.value = timerId
    }

    /**
     * Clear active timer.
     */
    fun clearActiveTimer() {
        prefs.edit().remove(KEY_ACTIVE_TIMER).apply()
        _activeTimerId.value = null
    }

    /**
     * Check if any alarm is ringing.
     */
    fun hasActiveAlarm(): Boolean = _activeAlarmId.value != null

    /**
     * Check if any timer is ringing.
     */
    fun hasActiveTimer(): Boolean = _activeTimerId.value != null

    companion object {
        private const val KEY_ACTIVE_ALARM = "active_alarm_id"
        private const val KEY_ACTIVE_TIMER = "active_timer_id"
    }
}
