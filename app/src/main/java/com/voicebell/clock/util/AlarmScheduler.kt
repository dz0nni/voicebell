package com.voicebell.clock.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for scheduling and canceling alarms using AlarmManager.
 *
 * This class wraps Android's AlarmManager API and handles:
 * - Exact alarm scheduling
 * - Pre-alarm scheduling
 * - Alarm cancellation
 * - Permission checks
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_IS_PRE_ALARM = "is_pre_alarm"
        const val EXTRA_PRE_ALARM_INDEX = "pre_alarm_index"

        // Pre-alarm IDs: mainAlarmId * 1000 + preAlarmIndex
        private const val PRE_ALARM_ID_MULTIPLIER = 1000
    }

    /**
     * Schedule an alarm with AlarmManager.
     *
     * @param alarm The alarm to schedule
     * @throws SecurityException if exact alarm permission is not granted
     */
    fun scheduleAlarm(alarm: Alarm) {
        // Check permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                throw SecurityException("Cannot schedule exact alarms. Permission not granted.")
            }
        }

        // Cancel existing alarm first (to update)
        cancelAlarm(alarm.id)

        // Don't schedule if disabled
        if (!alarm.isEnabled) {
            return
        }

        // Get next trigger time
        val triggerTime = alarm.getNextTriggerTime()
        if (triggerTime <= 0) {
            return
        }

        // Schedule main alarm
        scheduleAlarmInternal(alarm.id.toInt(), triggerTime, alarm.id, false, 0)

        // Schedule pre-alarms if configured
        if (alarm.preAlarmCount > 0) {
            schedulePreAlarms(alarm)
        }
    }

    /**
     * Schedule pre-alarms for an alarm.
     */
    private fun schedulePreAlarms(alarm: Alarm) {
        val mainTriggerTime = alarm.getNextTriggerTime()
        val intervalMillis = alarm.preAlarmInterval * 60 * 1000L

        for (i in 1..alarm.preAlarmCount) {
            val preAlarmTime = mainTriggerTime - (i * intervalMillis)

            // Don't schedule pre-alarm if it's in the past
            if (preAlarmTime > System.currentTimeMillis()) {
                val preAlarmId = getPreAlarmRequestCode(alarm.id, i)
                scheduleAlarmInternal(preAlarmId, preAlarmTime, alarm.id, true, i)
            }
        }
    }

    /**
     * Internal method to schedule a single alarm or pre-alarm.
     */
    private fun scheduleAlarmInternal(
        requestCode: Int,
        triggerTime: Long,
        alarmId: Long,
        isPreAlarm: Boolean,
        preAlarmIndex: Int
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_PRE_ALARM, isPreAlarm)
            putExtra(EXTRA_PRE_ALARM_INDEX, preAlarmIndex)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use setAlarmClock to show alarm in status bar and bypass Doze mode
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            getPendingIntentForShowAlarm()
        )

        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            // Handle permission not granted
            throw e
        }
    }

    /**
     * Cancel an alarm and all its pre-alarms.
     */
    fun cancelAlarm(alarmId: Long) {
        // Cancel main alarm
        cancelAlarmInternal(alarmId.toInt())

        // Cancel all possible pre-alarms (max 10)
        for (i in 1..10) {
            val preAlarmId = getPreAlarmRequestCode(alarmId, i)
            cancelAlarmInternal(preAlarmId)
        }
    }

    /**
     * Internal method to cancel a single alarm.
     */
    private fun cancelAlarmInternal(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    /**
     * Check if exact alarms can be scheduled.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Get PendingIntent for showing alarm list (used in AlarmClockInfo).
     */
    private fun getPendingIntentForShowAlarm(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Calculate request code for pre-alarm.
     */
    private fun getPreAlarmRequestCode(alarmId: Long, preAlarmIndex: Int): Int {
        return (alarmId * PRE_ALARM_ID_MULTIPLIER + preAlarmIndex).toInt()
    }
}
