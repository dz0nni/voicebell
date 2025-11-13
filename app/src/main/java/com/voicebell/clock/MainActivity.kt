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
                            onDismissAlarm = {
                                dismissAlarm()
                            },
                            onSnoozeAlarm = {
                                snoozeAlarm()
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
}

@Composable
fun ActiveServiceBanner(
    activeAlarmIdFlow: Flow<Long?>,
    onDismissAlarm: () -> Unit,
    onSnoozeAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAlarmId by activeAlarmIdFlow.collectAsState(initial = null)

    // Show banner only if there's an active alarm
    if (activeAlarmId != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⏰ ALARM IS RINGING!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Snooze button
                        Button(
                            onClick = onSnoozeAlarm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Snooze")
                        }

                        // Dismiss button
                        Button(
                            onClick = onDismissAlarm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}
