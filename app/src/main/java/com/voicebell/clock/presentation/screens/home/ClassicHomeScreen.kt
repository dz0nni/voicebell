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
    onNavigateToCreateAlarm: () -> Unit,
    onNavigateToEditAlarm: (Long) -> Unit,
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
                            1 -> "World Clocks"
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
                    onClick = { selectedTab = 0 }
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
                    onClick = { selectedTab = 1 }
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
                    onClick = { selectedTab = 2 }
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
                    onClick = { selectedTab = 3 }
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
                        onNavigateToCreateAlarm = onNavigateToCreateAlarm,
                        onNavigateToEditAlarm = onNavigateToEditAlarm,
                        onNavigateToSettings = {},
                        showTopBar = false // Hide top bar, ClassicHomeScreen already has one
                    )
                }
                1 -> {
                    WorldClocksScreen(
                        onNavigateBack = {} // No back navigation in classic mode
                    )
                }
                2 -> {
                    TimerScreen(
                        onNavigateToSettings = {},
                        showTopBar = false // Hide top bar, ClassicHomeScreen already has one
                    )
                }
                3 -> {
                    StopwatchScreen()
                }
            }
        }
    }
}
