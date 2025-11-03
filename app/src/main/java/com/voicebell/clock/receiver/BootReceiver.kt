package com.voicebell.clock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.voicebell.clock.worker.RescheduleAlarmsWorker

/**
 * BroadcastReceiver that handles device boot to reschedule alarms.
 *
 * When device boots, all alarms scheduled with AlarmManager are cleared.
 * This receiver reschedules all enabled alarms.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "Device boot detected, rescheduling alarms")

                // Use WorkManager to reschedule alarms in background
                val workRequest = OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>()
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}
