package com.voicebell.clock.service

import android.app.Notification
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
import com.voicebell.clock.domain.repository.TimerRepository
import com.voicebell.clock.presentation.screens.timer.TimerFinishedActivity
import com.voicebell.clock.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Foreground service for running timer countdown.
 * Updates notification every second with remaining time.
 */
@AndroidEntryPoint
class TimerService : Service() {

    companion object {
        private const val TAG = "TimerService"

        const val ACTION_START = "com.voicebell.clock.timer.START"
        const val ACTION_PAUSE = "com.voicebell.clock.timer.PAUSE"
        const val ACTION_RESUME = "com.voicebell.clock.timer.RESUME"
        const val ACTION_STOP = "com.voicebell.clock.timer.STOP"
        const val ACTION_FINISH = "com.voicebell.clock.timer.FINISH"

        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_DURATION_MILLIS = "duration_millis"
        const val EXTRA_LABEL = "label"

        private const val UPDATE_INTERVAL_MS = 1000L // Update every second
    }

    @Inject
    lateinit var timerRepository: TimerRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var updateJob: Job? = null
    private var currentTimerId: Long = -1
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        initializeVibrator()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1)
                if (timerId != -1L) {
                    startTimer(timerId)
                }
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_RESUME -> {
                resumeTimer()
            }
            ACTION_STOP -> {
                stopTimer()
            }
            ACTION_FINISH -> {
                finishTimer()
            }
        }

        return START_NOT_STICKY
    }

    private fun startTimer(timerId: Long) {
        Log.d(TAG, "Starting timer: $timerId")
        currentTimerId = timerId

        // Start foreground service
        val notification = createNotification(0, 0, false)
        startForeground(NotificationHelper.NOTIFICATION_ID_TIMER_SERVICE, notification)

        // Start countdown updates
        startCountdown()
    }

    private fun startCountdown() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                try {
                    val timer = timerRepository.getTimerById(currentTimerId)
                    if (timer == null) {
                        Log.w(TAG, "Timer not found: $currentTimerId")
                        stopSelf()
                        break
                    }

                    if (!timer.isRunning || timer.isPaused) {
                        // Timer paused, stop updating
                        break
                    }

                    val remaining = timer.getCurrentRemainingMillis()

                    if (remaining <= 0) {
                        // Timer finished!
                        handleTimerFinished(timer.vibrate)
                        break
                    }

                    // Update notification
                    val notification = createNotification(
                        remaining,
                        timer.durationMillis,
                        timer.isPaused
                    )
                    notificationHelper.notificationManager.notify(
                        NotificationHelper.NOTIFICATION_ID_TIMER_SERVICE,
                        notification
                    )

                    delay(UPDATE_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating timer", e)
                    break
                }
            }
        }
    }

    private fun pauseTimer() {
        Log.d(TAG, "Pausing timer")
        updateJob?.cancel()

        // Update notification to show paused state
        serviceScope.launch {
            val timer = timerRepository.getTimerById(currentTimerId)
            if (timer != null) {
                val notification = createNotification(
                    timer.remainingMillis,
                    timer.durationMillis,
                    true
                )
                notificationHelper.notificationManager.notify(
                    NotificationHelper.NOTIFICATION_ID_TIMER_SERVICE,
                    notification
                )
            }
        }
    }

    private fun resumeTimer() {
        Log.d(TAG, "Resuming timer")
        startCountdown()
    }

    private fun stopTimer() {
        Log.d(TAG, "Stopping timer")
        updateJob?.cancel()
        stopSelf()
    }

    private fun handleTimerFinished(vibrate: Boolean) {
        Log.d(TAG, "Timer finished!")

        serviceScope.launch {
            try {
                // Mark timer as finished in database
                timerRepository.markAsFinished(currentTimerId)

                // Play alarm sound
                playAlarmSound()

                // Vibrate if enabled
                if (vibrate) {
                    startVibration()
                }

                // Show finished notification
                val notification = createFinishedNotification()
                notificationHelper.notificationManager.notify(
                    NotificationHelper.NOTIFICATION_ID_TIMER_FINISHED,
                    notification
                )

                // Launch full-screen activity
                launchFinishedActivity()

                // Stop countdown but keep service running for alarm
                updateJob?.cancel()

                // Auto-stop alarm after 60 seconds
                delay(60000)
                stopAlarm()
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling timer finish", e)
                stopSelf()
            }
        }
    }

    private fun finishTimer() {
        Log.d(TAG, "Finishing timer (user dismissed)")
        stopAlarm()
        stopSelf()
    }

    private fun playAlarmSound() {
        try {
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
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
        }
    }

    private fun startVibration() {
        try {
            vibrator?.let {
                val pattern = longArrayOf(0, 1000, 500)
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

    private fun stopAlarm() {
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
        vibrator?.cancel()
    }

    private fun launchFinishedActivity() {
        val intent = Intent(this, TimerFinishedActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(EXTRA_TIMER_ID, currentTimerId)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching finished activity", e)
        }
    }

    private fun createNotification(
        remainingMillis: Long,
        totalMillis: Long,
        isPaused: Boolean
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pause/Resume action
        val pauseResumeAction = if (isPaused) {
            val resumeIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePendingIntent = PendingIntent.getService(
                this,
                1,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground, // TODO: Add proper icon
                "Resume",
                resumePendingIntent
            ).build()
        } else {
            val pauseIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                1,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground, // TODO: Add proper icon
                "Pause",
                pausePendingIntent
            ).build()
        }

        // Stop action
        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground, // TODO: Add proper icon
            "Stop",
            stopPendingIntent
        ).build()

        val timeText = formatTime(remainingMillis)
        val contentText = if (isPaused) "$timeText (Paused)" else timeText

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_TIMER_SERVICE)
            .setContentTitle("Timer")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Add proper icon
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .setProgress(
                totalMillis.toInt(),
                (totalMillis - remainingMillis).toInt(),
                false
            )
            .build()
    }

    private fun createFinishedNotification(): Notification {
        val finishIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_FINISH
        }
        val finishPendingIntent = PendingIntent.getService(
            this,
            3,
            finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_TIMER_FINISHED)
            .setContentTitle("Timer Finished!")
            .setContentText("Tap to stop alarm")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Add proper icon
            .setContentIntent(finishPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(finishPendingIntent, true)
            .build()
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 60000) % 60
        val hours = millis / 3600000

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
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
        updateJob?.cancel()
        stopAlarm()
        serviceScope.cancel()
    }
}
