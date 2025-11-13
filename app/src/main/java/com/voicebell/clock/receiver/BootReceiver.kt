package com.voicebell.clock.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.voicebell.clock.MainActivity
import com.voicebell.clock.R
import com.voicebell.clock.util.NotificationHelper
import com.voicebell.clock.worker.RescheduleAlarmsWorker

/**
 * BroadcastReceiver that handles device boot to reschedule alarms.
 *
 * When device boots, all alarms scheduled with AlarmManager are cleared.
 * This receiver reschedules all enabled alarms.
 *
 * Also handles MY_PACKAGE_REPLACED to activate full-screen intent permissions
 * on Android 14+ after app updates.
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
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Device boot detected, rescheduling alarms")

                // Use WorkManager to reschedule alarms in background
                val workRequest = OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>()
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }

            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "App updated, rescheduling alarms and activating full-screen intent")

                // Reschedule alarms
                val workRequest = OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>()
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)

                // Activate full-screen intent permission on Android 14+
                // This is required because Android 14+ needs the app to post a notification
                // with full-screen intent at least once before it will work
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    activateFullScreenIntent(context)
                }
            }
        }
    }

    /**
     * Activates full-screen intent permission by posting and immediately canceling
     * a test notification. This is required on Android 14+ after app updates.
     */
    private fun activateFullScreenIntent(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Ensure alarm service channel exists
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NotificationHelper.CHANNEL_ID_ALARM_SERVICE,
                    "Alarm Service",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Foreground service for alarm notifications"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Create pending intent for full-screen notification
            val fullScreenIntent = Intent(context, MainActivity::class.java)
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                999,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create and post notification with full-screen intent
            val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_ALARM_SERVICE)
                .setContentTitle("VoiceBell Updated")
                .setContentText("Activating alarm permissions...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setTimeoutAfter(100) // Auto-dismiss after 100ms
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build()

            // Post notification to activate full-screen intent capability
            notificationManager.notify(999, notification)

            // Cancel after a short delay to ensure Android registers it
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                notificationManager.cancel(999)
                Log.d(TAG, "Full-screen intent activated successfully")
            }, 200)

        } catch (e: Exception) {
            Log.e(TAG, "Error activating full-screen intent", e)
        }
    }
}
