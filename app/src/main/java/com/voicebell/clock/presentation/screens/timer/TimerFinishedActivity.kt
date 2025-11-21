package com.voicebell.clock.presentation.screens.timer

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
import com.voicebell.clock.service.VoiceRecognitionService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen activity shown when timer finishes.
 * Automatically starts voice recognition to listen for "STOP" command.
 * Activity being in foreground enables microphone access (Android 14+ compliance).
 */
@AndroidEntryPoint
class TimerFinishedActivity : ComponentActivity() {

    private var timerId: Long = -1
    private var voiceResultReceiver: BroadcastReceiver? = null
    private var isReceiverRegistered = false

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

        // Register receiver to listen for voice recognition results
        registerVoiceResultReceiver()

        // Start voice recognition (activity is foreground → microphone works!)
        Log.d(TAG, "Starting voice recognition for timer $timerId")
        VoiceRecognitionService.startListening(this, listenForStopCommand = true)

        setContent {
            VoiceBellTheme {
                TimerFinishedScreen(
                    onDismiss = { dismissTimer() }
                )
            }
        }
    }

    private fun registerVoiceResultReceiver() {
        voiceResultReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == VoiceRecognitionService.ACTION_RESULT) {
                    val text = intent.getStringExtra(VoiceRecognitionService.EXTRA_RESULT_TEXT) ?: ""
                    val success = intent.getBooleanExtra(VoiceRecognitionService.EXTRA_RESULT_SUCCESS, false)

                    Log.d(TAG, "Voice result received - text: $text, success: $success")

                    if (success && text.lowercase().contains("stop")) {
                        Log.d(TAG, "STOP command detected, dismissing timer")
                        dismissTimer()
                    }
                }
            }
        }

        val filter = IntentFilter(VoiceRecognitionService.ACTION_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(voiceResultReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(voiceResultReceiver, filter)
        }
        isReceiverRegistered = true

        Log.d(TAG, "Voice result receiver registered")
    }

    private fun unregisterVoiceResultReceiver() {
        if (isReceiverRegistered && voiceResultReceiver != null) {
            try {
                unregisterReceiver(voiceResultReceiver)
                isReceiverRegistered = false
                voiceResultReceiver = null
                Log.d(TAG, "Voice result receiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
    }

    private fun dismissTimer() {
        // Unregister receiver
        unregisterVoiceResultReceiver()

        // Stop voice recognition
        VoiceRecognitionService.stopListening(this)

        // Stop the alarm service
        val intent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_FINISH
            putExtra(TimerService.EXTRA_TIMER_ID, timerId)
        }
        startService(intent)

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterVoiceResultReceiver()
        VoiceRecognitionService.stopListening(this)
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
