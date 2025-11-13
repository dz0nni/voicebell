package com.voicebell.clock.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.voicebell.clock.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating and managing notifications.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_ALARM = "alarm_channel"
        const val CHANNEL_ID_TIMER = "timer_channel"
        const val CHANNEL_ID_ALARM_SERVICE = "alarm_service_channel"
        const val CHANNEL_ID_TIMER_SERVICE = "timer_service_channel"
        const val CHANNEL_ID_TIMER_FINISHED = "timer_finished_channel"
        const val CHANNEL_ID_VOICE_SERVICE = "voice_service_channel"

        const val NOTIFICATION_ID_ALARM = 1001
        const val NOTIFICATION_ID_TIMER = 1002
        const val NOTIFICATION_ID_ALARM_SERVICE = 1003
        const val NOTIFICATION_ID_TIMER_SERVICE = 1004
        const val NOTIFICATION_ID_TIMER_FINISHED = 1005
        const val NOTIFICATION_ID_VOICE_SERVICE = 1006
    }

    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Create all notification channels (Android 8.0+).
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Alarm channel
            val alarmChannel = NotificationChannel(
                CHANNEL_ID_ALARM,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true) // Allow alarms to bypass Do Not Disturb
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Timer channel
            val timerChannel = NotificationChannel(
                CHANNEL_ID_TIMER,
                "Timers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timer notifications"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }

            // Alarm service channel (for foreground service)
            val alarmServiceChannel = NotificationChannel(
                CHANNEL_ID_ALARM_SERVICE,
                "Alarm Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification shown when alarm is ringing"
                setShowBadge(false)
            }

            // Timer service channel (for timer countdown)
            val timerServiceChannel = NotificationChannel(
                CHANNEL_ID_TIMER_SERVICE,
                "Timer Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Notification shown during timer countdown"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            // Timer finished channel (for timer completion alert)
            val timerFinishedChannel = NotificationChannel(
                CHANNEL_ID_TIMER_FINISHED,
                "Timer Finished",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alert when timer finishes"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Voice service channel (for voice recognition)
            val voiceServiceChannel = NotificationChannel(
                CHANNEL_ID_VOICE_SERVICE,
                "Voice Recognition",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Notification shown during voice recognition"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(
                listOf(
                    alarmChannel,
                    timerChannel,
                    alarmServiceChannel,
                    timerServiceChannel,
                    timerFinishedChannel,
                    voiceServiceChannel
                )
            )
        }
    }

}
