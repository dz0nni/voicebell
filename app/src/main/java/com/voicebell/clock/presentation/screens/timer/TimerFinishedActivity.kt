package com.voicebell.clock.presentation.screens.timer

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voicebell.clock.presentation.theme.VoiceBellTheme
import com.voicebell.clock.service.TimerService
import com.voicebell.clock.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen activity shown when timer finishes.
 * Automatically starts voice recognition to listen for "STOP" command.
 * Activity being in foreground enables microphone access (Android 14+ compliance).
 */
@AndroidEntryPoint
class TimerFinishedActivity : ComponentActivity() {

    private var timerId: Long = -1
    private var dismissedReceiver: BroadcastReceiver? = null

    companion object {
        private const val TAG = "TimerFinishedActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lockscreen and turn on screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Get timer ID from intent
        timerId = intent.getLongExtra(TimerService.EXTRA_TIMER_ID, -1)
        Log.d(TAG, "TimerFinishedActivity created for timer $timerId")

        // Register receiver to listen for timer dismissed broadcast (from voice command)
        registerDismissedReceiver()

        // Note: Voice recognition is handled by TimerService directly
        // We don't start VoiceRecognitionService here to avoid conflicts

        setContent {
            VoiceBellTheme {
                TimerFinishedScreen(
                    onDismiss = { dismissTimer() }
                )
            }
        }
    }

    private fun registerDismissedReceiver() {
        dismissedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == TimerService.ACTION_TIMER_DISMISSED) {
                    val dismissedTimerId = intent.getLongExtra(TimerService.EXTRA_TIMER_ID, -1)
                    Log.d(TAG, "Received timer dismissed broadcast for timer: $dismissedTimerId")
                    if (dismissedTimerId == timerId || dismissedTimerId == -1L) {
                        Log.d(TAG, "Closing activity due to voice dismiss")
                        finish()
                    }
                }
            }
        }

        val filter = IntentFilter(TimerService.ACTION_TIMER_DISMISSED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dismissedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(dismissedReceiver, filter)
        }
        Log.d(TAG, "Registered dismissed receiver")
    }

    private fun unregisterDismissedReceiver() {
        dismissedReceiver?.let {
            try {
                unregisterReceiver(it)
                Log.d(TAG, "Unregistered dismissed receiver")
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering receiver", e)
            }
        }
        dismissedReceiver = null
    }

    private fun dismissTimer() {
        Log.d(TAG, "Dismissing timer: $timerId")

        // Unregister receiver first
        unregisterDismissedReceiver()

        // Cancel the notification directly
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = NotificationHelper.NOTIFICATION_ID_TIMER_FINISHED + timerId.toInt()
        notificationManager.cancel(notificationId)
        Log.d(TAG, "Cancelled notification: $notificationId")

        // Stop the alarm via TimerService (this also stops voice recognition)
        val intent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_FINISH
            putExtra(TimerService.EXTRA_TIMER_ID, timerId)
        }
        startService(intent)

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterDismissedReceiver()
    }

    override fun onBackPressed() {
        // Prevent back button from closing
        // User must explicitly dismiss
    }
}

@Composable
fun TimerFinishedScreen(
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Timer icon
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Timer Finished!",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your timer has completed",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Dismiss button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
