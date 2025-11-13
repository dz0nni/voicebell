package com.voicebell.clock.presentation.screens.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.presentation.theme.VoiceBellTheme
import com.voicebell.clock.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Full-screen activity shown when alarm is ringing.
 *
 * Features:
 * - Shows current time and alarm label
 * - Dismiss button
 * - Snooze button
 * - Bypasses lockscreen and turns on screen
 */
@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    private val viewModel: AlarmRingingViewModel by viewModels()
    private var isPreAlarm: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lockscreen and turn on screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Get alarm data from intent
        isPreAlarm = intent.getBooleanExtra(AlarmService.EXTRA_IS_PRE_ALARM, false)

        setContent {
            VoiceBellTheme {
                val alarmLabel by viewModel.alarmLabel.collectAsStateWithLifecycle()

                AlarmRingingScreen(
                    alarmLabel = alarmLabel,
                    isPreAlarm = isPreAlarm,
                    onDismiss = { dismissAlarm() },
                    onSnooze = { snoozeAlarm() }
                )
            }
        }
    }

    private fun dismissAlarm() {
        // Send dismiss intent to service
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_DISMISS_ALARM
        }
        startService(intent)

        finish()
    }

    private fun snoozeAlarm() {
        // Send snooze intent to service
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE_ALARM
        }
        startService(intent)

        finish()
    }

    override fun onBackPressed() {
        // Prevent back button from closing alarm
        // User must explicitly dismiss or snooze
    }
}

@Composable
fun AlarmRingingScreen(
    alarmLabel: String,
    isPreAlarm: Boolean,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
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

            // Alarm type indicator
            Text(
                text = if (isPreAlarm) "Pre-Alarm" else "Alarm",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Current time
            val currentTime = remember {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                LocalTime.now().format(formatter)
            }

            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display alarm label if available
            if (alarmLabel.isNotBlank()) {
                Text(
                    text = alarmLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

                // Snooze button
                if (!isPreAlarm) {
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text(
                            text = "Snooze",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
