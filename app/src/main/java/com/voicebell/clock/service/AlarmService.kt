package com.voicebell.clock.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.voicebell.clock.R
import com.voicebell.clock.domain.repository.AlarmRepository
import com.voicebell.clock.presentation.screens.alarm.AlarmRingingActivity
import com.voicebell.clock.receiver.AlarmReceiver
import com.voicebell.clock.util.AlarmScheduler
import com.voicebell.clock.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for playing alarm sound.
 *
 * This service:
 * - Plays alarm sound with gradual volume increase
 * - Handles vibration
 * - Shows full-screen alarm activity
 * - Runs as foreground service for reliability
 */
@AndroidEntryPoint
class AlarmService : Service() {

    companion object {
        private const val TAG = "AlarmService"

        const val ACTION_START_ALARM = "com.voicebell.clock.START_ALARM"
        const val ACTION_DISMISS_ALARM = "com.voicebell.clock.DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.voicebell.clock.SNOOZE_ALARM"

        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_IS_PRE_ALARM = "is_pre_alarm"
        const val EXTRA_PRE_ALARM_INDEX = "pre_alarm_index"

        private const val GRADUAL_VOLUME_DURATION_MS = 60000L // 60 seconds
        private const val VOLUME_INCREASE_STEPS = 20
    }

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var flashManager: FlashManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentAlarmId: Long = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        initializeVibrator()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)
                val isPreAlarm = intent.getBooleanExtra(EXTRA_IS_PRE_ALARM, false)

                if (alarmId != -1L) {
                    currentAlarmId = alarmId
                    startAlarm(alarmId, isPreAlarm)
                } else {
                    stopSelf()
                }
            }
            ACTION_DISMISS_ALARM -> {
                dismissAlarm()
            }
            ACTION_SNOOZE_ALARM -> {
                snoozeAlarm()
            }
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startAlarm(alarmId: Long, isPreAlarm: Boolean) {
        Log.d(TAG, "Starting alarm: id=$alarmId, isPreAlarm=$isPreAlarm")

        // Start foreground service
        val notification = createForegroundNotification(alarmId, isPreAlarm)
        startForeground(NotificationHelper.NOTIFICATION_ID_ALARM_SERVICE, notification)

        // Launch full-screen alarm activity
        launchAlarmActivity(alarmId, isPreAlarm)

        // Load alarm from database and start ringing
        serviceScope.launch {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    startRinging(
                        gradualVolume = alarm.gradualVolumeIncrease,
                        shouldVibrate = alarm.vibrate,
                        volumeLevel = alarm.volumeLevel,
                        shouldFlash = alarm.flash
                    )
                } else {
                    Log.e(TAG, "Alarm not found: $alarmId")
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading alarm", e)
                stopSelf()
            }
        }
    }

    private fun startRinging(
        gradualVolume: Boolean,
        shouldVibrate: Boolean,
        volumeLevel: Int,
        shouldFlash: Boolean
    ) {
        // Start alarm sound
        startAlarmSound(gradualVolume, volumeLevel)

        // Start vibration
        if (shouldVibrate) {
            startVibration()
        }

        // Start flash
        if (shouldFlash && flashManager.hasFlash()) {
            flashManager.startFlashing(serviceScope)
        }
    }

    private fun startAlarmSound(gradualVolume: Boolean, volumeLevel: Int) {
        try {
            // Get default alarm sound
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                isLooping = true

                // Set initial volume
                if (gradualVolume) {
                    setVolume(0f, 0f)
                } else {
                    val volume = volumeLevel / 100f
                    setVolume(volume, volume)
                }

                prepare()
                start()
            }

            // Gradual volume increase
            if (gradualVolume) {
                startGradualVolumeIncrease(volumeLevel)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting alarm sound", e)
        }
    }

    private fun startGradualVolumeIncrease(targetVolumeLevel: Int) {
        val targetVolume = targetVolumeLevel / 100f
        val stepDuration = GRADUAL_VOLUME_DURATION_MS / VOLUME_INCREASE_STEPS
        val volumeIncrement = targetVolume / VOLUME_INCREASE_STEPS

        serviceScope.launch {
            repeat(VOLUME_INCREASE_STEPS) { step ->
                delay(stepDuration)
                val currentVolume = (step + 1) * volumeIncrement
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            }
        }
    }

    private fun startVibration() {
        try {
            vibrator?.let {
                val pattern = longArrayOf(0, 1000, 500) // 0ms wait, 1s vibrate, 0.5s pause
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    it.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }

    private fun stopRinging() {
        // Stop media player
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping media player", e)
            }
        }
        mediaPlayer = null

        // Stop vibration
        vibrator?.cancel()

        // Stop flash
        flashManager.stopFlashing()
    }

    private fun launchAlarmActivity(alarmId: Long, isPreAlarm: Boolean) {
        val intent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_PRE_ALARM, isPreAlarm)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching alarm activity", e)
        }
    }

    private fun createForegroundNotification(alarmId: Long, isPreAlarm: Boolean): Notification {
        val contentIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_PRE_ALARM, isPreAlarm)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPreAlarm) "Pre-Alarm" else "Alarm"
        val content = "Tap to view"

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_ALARM_SERVICE)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Add proper icon
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Enable full-screen intent for locked screen
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    private fun dismissAlarm() {
        Log.d(TAG, "Dismissing alarm")

        // Reset snooze count
        serviceScope.launch {
            if (currentAlarmId != -1L) {
                alarmRepository.resetSnoozeCount(currentAlarmId)
            }
        }

        stopRinging()
        stopSelf()
    }

    private fun snoozeAlarm() {
        Log.d(TAG, "Snoozing alarm")

        serviceScope.launch {
            if (currentAlarmId != -1L) {
                try {
                    val alarm = alarmRepository.getAlarmById(currentAlarmId)
                    if (alarm != null) {
                        // Check if snooze is enabled
                        if (!alarm.snoozeEnabled) {
                            Log.w(TAG, "Snooze not enabled for alarm $currentAlarmId")
                            dismissAlarm()
                            return@launch
                        }

                        // Check if max snooze count reached
                        if (alarm.snoozeCount >= alarm.maxSnoozeCount) {
                            Log.w(TAG, "Max snooze count reached for alarm $currentAlarmId")
                            dismissAlarm()
                            return@launch
                        }

                        // Increment snooze count
                        val newSnoozeCount = alarm.snoozeCount + 1
                        alarmRepository.updateSnoozeCount(currentAlarmId, newSnoozeCount)

                        // Schedule snooze alarm
                        val snoozeTimeMillis = System.currentTimeMillis() +
                            (alarm.snoozeDuration * 60 * 1000L)

                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this@AlarmService, AlarmReceiver::class.java).apply {
                            putExtra(AlarmScheduler.EXTRA_ALARM_ID, currentAlarmId)
                            putExtra(AlarmScheduler.EXTRA_IS_PRE_ALARM, false)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            this@AlarmService,
                            currentAlarmId.toInt(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        // Use setAlarmClock for snooze to show in status bar
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(
                            snoozeTimeMillis,
                            pendingIntent
                        )
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

                        Log.d(TAG, "Alarm snoozed for ${alarm.snoozeDuration} minutes (count: $newSnoozeCount/${alarm.maxSnoozeCount})")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error snoozing alarm", e)
                }
            }
        }

        stopRinging()
        stopSelf()
    }

    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
        flashManager.release()
        serviceScope.cancel()
    }
}
