package com.voicebell.clock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.voicebell.clock.service.AlarmService
import com.voicebell.clock.util.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver that handles alarm triggers from AlarmManager.
 *
 * When an alarm fires, this receiver:
 * 1. Starts AlarmService to play sound
 * 2. Launches AlarmRingingActivity for UI
 * 3. Handles pre-alarms differently than main alarms
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val isPreAlarm = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_PRE_ALARM, false)
        val preAlarmIndex = intent.getIntExtra(AlarmScheduler.EXTRA_PRE_ALARM_INDEX, 0)

        if (alarmId == -1L) {
            Log.e(TAG, "Invalid alarm ID received")
            return
        }

        Log.d(TAG, "Alarm triggered: id=$alarmId, isPreAlarm=$isPreAlarm, index=$preAlarmIndex")

        // Start AlarmService to handle ringing
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_IS_PRE_ALARM, isPreAlarm)
            putExtra(AlarmService.EXTRA_PRE_ALARM_INDEX, preAlarmIndex)
        }

        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AlarmService", e)
        }
    }
}
