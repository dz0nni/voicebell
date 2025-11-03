package com.voicebell.clock.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.voicebell.clock.presentation.screens.alarm.AlarmScreen
import com.voicebell.clock.presentation.screens.timer.TimerScreen
import com.voicebell.clock.presentation.screens.worldclock.WorldClocksScreen
import com.voicebell.clock.presentation.stopwatch.StopwatchScreen

/**
 * Classic home screen with bottom navigation tabs:
 * - Alarm
 * - Clock (World clocks)
 * - Timer
 * - Stopwatch
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicHomeScreen(
    onNavigateToAlarms: () -> Unit,
    onNavigateToWorldClocks: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToStopwatch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Alarms"
                            1 -> "Clock"
                            2 -> "Timer"
                            3 -> "Stopwatch"
                            else -> "VoiceBell"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null
                        )
                    },
                    label = { Text("Alarm") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        onNavigateToAlarms()
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null
                        )
                    },
                    label = { Text("Clock") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToWorldClocks()
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null
                        )
                    },
                    label = { Text("Timer") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToTimer()
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null
                        )
                    },
                    label = { Text("Stopwatch") },
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onNavigateToStopwatch()
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    AlarmScreen(
                        onNavigateToEdit = { alarmId ->
                            // Navigate handled by parent
                        },
                        onNavigateBack = { /* No back from home */ }
                    )
                }
                1 -> {
                    WorldClocksScreen(
                        onNavigateBack = { /* No back from home */ }
                    )
                }
                2 -> {
                    TimerScreen(
                        onNavigateBack = { /* No back from home */ }
                    )
                }
                3 -> {
                    StopwatchScreen()
                }
            }
        }
    }
}
