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
import android.os.PowerManager
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

    @Inject
    lateinit var activeServiceManager: com.voicebell.clock.util.ActiveServiceManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val updateJobs = mutableMapOf<Long, Job>()  // Track multiple timer jobs
    private val mediaPlayers = mutableMapOf<Long, MediaPlayer>()  // Track multiple alarms
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
                val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1)
                if (timerId != -1L) {
                    pauseTimer(timerId)
                }
            }
            ACTION_RESUME -> {
                val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1)
                if (timerId != -1L) {
                    resumeTimer(timerId)
                }
            }
            ACTION_STOP -> {
                val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1)
                if (timerId != -1L) {
                    stopTimer(timerId)
                }
            }
            ACTION_FINISH -> {
                val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1)
                if (timerId != -1L) {
                    finishTimer(timerId)
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun startTimer(timerId: Long) {
        Log.d(TAG, "Starting timer: $timerId")

        // Start foreground service with first timer
        if (updateJobs.isEmpty()) {
            val notification = createNotification(timerId, 0, 0, false)
            startForeground(NotificationHelper.NOTIFICATION_ID_TIMER_SERVICE, notification)
        }

        // Start countdown updates for this specific timer
        startCountdown(timerId)
    }

    private fun startCountdown(timerId: Long) {
        // Cancel existing job for this timer if any
        updateJobs[timerId]?.cancel()

        // Create new countdown job for this timer
        updateJobs[timerId] = serviceScope.launch {
            while (isActive) {
                try {
                    val timer = timerRepository.getTimerById(timerId)
                    if (timer == null) {
                        Log.w(TAG, "Timer not found: $timerId")
                        updateJobs.remove(timerId)
                        stopSelfIfNoActiveTimers()
                        break
                    }

                    if (!timer.isRunning || timer.isPaused) {
                        // Timer paused, stop updating
                        break
                    }

                    val remaining = timer.getCurrentRemainingMillis()

                    if (remaining <= 0) {
                        // Timer finished!
                        handleTimerFinished(timerId, timer.vibrate)
                        break
                    }

                    // Update notification (show first active timer)
                    if (updateJobs.keys.firstOrNull() == timerId) {
                        val notification = createNotification(
                            timerId,
                            remaining,
                            timer.durationMillis,
                            timer.isPaused
                        )
                        notificationHelper.notificationManager.notify(
                            NotificationHelper.NOTIFICATION_ID_TIMER_SERVICE,
                            notification
                        )
                    }

                    delay(UPDATE_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating timer $timerId", e)
                    updateJobs.remove(timerId)
                    break
                }
            }
        }
    }

    private fun pauseTimer(timerId: Long) {
        Log.d(TAG, "Pausing timer: $timerId")
        updateJobs[timerId]?.cancel()
        updateJobs.remove(timerId)

        // Update notification to show paused state
        serviceScope.launch {
            val timer = timerRepository.getTimerById(timerId)
            if (timer != null) {
                val notification = createNotification(
                    timerId,
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

    private fun resumeTimer(timerId: Long) {
        Log.d(TAG, "Resuming timer: $timerId")
        startCountdown(timerId)
    }

    private fun stopTimer(timerId: Long) {
        Log.d(TAG, "Stopping timer: $timerId")
        updateJobs[timerId]?.cancel()
        updateJobs.remove(timerId)
        stopSelfIfNoActiveTimers()
    }

    private fun handleTimerFinished(timerId: Long, vibrate: Boolean) {
        Log.d(TAG, "Timer finished: $timerId")

        // Mark timer as active (ringing)
        activeServiceManager.setTimerActive(timerId)

        serviceScope.launch {
            try {
                // Mark timer as finished in database
                timerRepository.markAsFinished(timerId)

                // Play alarm sound for this specific timer
                playAlarmSound(timerId)

                // Vibrate if enabled
                if (vibrate) {
                    startVibration()
                }

                // Start voice recognition service to listen for "stop" command
                // This works regardless of whether full-screen activity or heads-up notification is shown
                try {
                    VoiceRecognitionService.startListening(applicationContext, listenForStopCommand = true)
                    Log.d(TAG, "Started VoiceRecognitionService to listen for STOP command")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to start VoiceRecognitionService: ${e.message}")
                }

                // Post notification with Dismiss button (for both lock screen and heads-up)
                val notification = createFinishedNotification(timerId)
                notificationHelper.notificationManager.notify(
                    NotificationHelper.NOTIFICATION_ID_TIMER_FINISHED + timerId.toInt(),
                    notification
                )
                Log.d(TAG, "Posted timer finished notification for timer: $timerId")

                // TimerFinishedActivity will handle voice recognition in its onCreate()

                // Remove from active jobs
                updateJobs.remove(timerId)

                // Auto-stop alarm after 60 seconds
                delay(60000)
                Log.d(TAG, "Auto-stopping timer alarm: $timerId")
                stopAlarm(timerId)
                // Dismiss notification
                notificationHelper.notificationManager.cancel(
                    NotificationHelper.NOTIFICATION_ID_TIMER_FINISHED + timerId.toInt()
                )
                activeServiceManager.clearActiveTimer()
                stopSelfIfNoActiveTimers()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling timer finish", e)
                stopSelfIfNoActiveTimers()
            }
        }
    }

    private fun finishTimer(timerId: Long) {
        // If timerId is -1, stop the currently active timer (from voice command)
        val actualTimerId = if (timerId == -1L) {
            activeServiceManager.activeTimerId.value ?: run {
                Log.w(TAG, "No active timer to finish")
                return
            }
        } else {
            timerId
        }

        Log.d(TAG, "Finishing timer (user dismissed): $actualTimerId")

        // Clear active timer
        activeServiceManager.clearActiveTimer()

        // Stop alarm and dismiss notification
        stopAlarm(actualTimerId)
        notificationHelper.notificationManager.cancel(
            NotificationHelper.NOTIFICATION_ID_TIMER_FINISHED + actualTimerId.toInt()
        )

        // Stop voice recognition service if running
        try {
            VoiceRecognitionService.stopListening(applicationContext)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop VoiceRecognitionService", e)
        }

        stopSelfIfNoActiveTimers()
    }

    private fun playAlarmSound(timerId: Long) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayers[timerId] = MediaPlayer().apply {
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
            Log.e(TAG, "Error playing alarm sound for timer $timerId", e)
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

    private fun stopAlarm(timerId: Long) {
        mediaPlayers[timerId]?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping media player for timer $timerId", e)
            }
        }
        mediaPlayers.remove(timerId)

        // Only stop vibration if no other alarms are playing
        if (mediaPlayers.isEmpty()) {
            vibrator?.cancel()
        }
    }

    private fun stopSelfIfNoActiveTimers() {
        // Only stop service if no timers are running and no alarms are playing
        if (updateJobs.isEmpty() && mediaPlayers.isEmpty()) {
            Log.d(TAG, "No active timers, stopping service")
            stopSelf()
        }
    }

    private fun createNotification(
        timerId: Long,
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
                putExtra(EXTRA_TIMER_ID, timerId)
            }
            val resumePendingIntent = PendingIntent.getService(
                this,
                timerId.toInt() * 10 + 1,
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
                putExtra(EXTRA_TIMER_ID, timerId)
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                timerId.toInt() * 10 + 1,
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
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            timerId.toInt() * 10 + 2,
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

    private fun createFinishedNotification(timerId: Long): Notification {
        // Full-screen intent opens TimerFinishedActivity (enables voice recognition)
        val activityIntent = Intent(this, com.voicebell.clock.presentation.screens.timer.TimerFinishedActivity::class.java).apply {
            putExtra(EXTRA_TIMER_ID, timerId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            timerId.toInt(), // Unique request code per timer
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action - stops alarm directly from notification
        val dismissIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_FINISH
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            timerId.toInt() + 1000, // Different request code
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_TIMER_FINISHED)
            .setContentTitle("Timer Finished!")
            .setContentText("Say 'STOP' or tap to dismiss")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Add proper icon
            .setContentIntent(activityPendingIntent)
            .setFullScreenIntent(activityPendingIntent, true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibrate, lights for heads-up
            .addAction(0, "Dismiss", dismissPendingIntent) // Dismiss button
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

        // Cancel all countdown jobs
        updateJobs.values.forEach { it.cancel() }
        updateJobs.clear()

        // Stop all alarms
        mediaPlayers.keys.toList().forEach { timerId ->
            stopAlarm(timerId)
        }

        serviceScope.cancel()
    }
}
