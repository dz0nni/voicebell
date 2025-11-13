package com.voicebell.clock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.voicebell.clock.presentation.navigation.NavGraph
import com.voicebell.clock.presentation.theme.VoiceBellTheme
import com.voicebell.clock.service.AlarmService
import com.voicebell.clock.util.ActiveServiceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Main Activity for VoiceBell.
 *
 * This is the single activity that hosts all Compose screens.
 * Uses AndroidEntryPoint annotation for Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activeServiceManager: ActiveServiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VoiceBellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Active alarm/timer banner overlay
                        ActiveServiceBanner(
                            activeAlarmIdFlow = activeServiceManager.activeAlarmId,
                            activeTimerIdFlow = activeServiceManager.activeTimerId,
                            onDismissAlarm = {
                                dismissAlarm()
                            },
                            onSnoozeAlarm = {
                                snoozeAlarm()
                            },
                            onStopTimer = {
                                stopTimer()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun dismissAlarm() {
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_DISMISS_ALARM
        }
        startService(intent)
    }

    private fun snoozeAlarm() {
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE_ALARM
        }
        startService(intent)
    }

    private fun stopTimer() {
        val intent = Intent(this, com.voicebell.clock.service.TimerService::class.java).apply {
            action = com.voicebell.clock.service.TimerService.ACTION_FINISH
        }
        startService(intent)
    }
}

@Composable
fun ActiveServiceBanner(
    activeAlarmIdFlow: Flow<Long?>,
    activeTimerIdFlow: Flow<Long?>,
    onDismissAlarm: () -> Unit,
    onSnoozeAlarm: () -> Unit,
    onStopTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAlarmId by activeAlarmIdFlow.collectAsState(initial = null)
    val activeTimerId by activeTimerIdFlow.collectAsState(initial = null)

    // Show banner if there's an active alarm or timer
    if (activeAlarmId != null || activeTimerId != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Show alarm or timer specific UI
                    if (activeAlarmId != null) {
                        // Alarm UI
                        Text(
                            text = "⏰",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = "ALARM IS RINGING!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Dismiss button (primary action)
                            Button(
                                onClick = onDismissAlarm,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Dismiss",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }

                            // Snooze button (secondary action)
                            OutlinedButton(
                                onClick = onSnoozeAlarm,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                            ) {
                                Text(
                                    text = "Snooze",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    } else {
                        // Timer UI
                        Text(
                            text = "⏱️",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = "TIMER FINISHED!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        // Stop button
                        Button(
                            onClick = onStopTimer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Stop",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
