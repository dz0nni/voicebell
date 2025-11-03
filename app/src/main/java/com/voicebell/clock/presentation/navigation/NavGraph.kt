package com.voicebell.clock.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voicebell.clock.presentation.screens.alarm.AlarmScreen
import com.voicebell.clock.presentation.screens.alarm.edit.AlarmEditScreen
import com.voicebell.clock.presentation.screens.home.MainScreen
import com.voicebell.clock.presentation.screens.settings.SettingsScreen
import com.voicebell.clock.presentation.screens.timer.TimerScreen
import com.voicebell.clock.presentation.screens.voice.VoiceCommandScreen
import com.voicebell.clock.presentation.screens.worldclock.WorldClocksScreen
import com.voicebell.clock.presentation.stopwatch.StopwatchScreen

/**
 * Main navigation graph for VoiceBell.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        // Home screen (switches between Classic and Experimental modes)
        composable(Routes.Home.route) {
            MainScreen(
                onNavigateToAlarms = {
                    navController.navigate(Routes.Alarms.route)
                },
                onNavigateToWorldClocks = {
                    navController.navigate(Routes.WorldClocks.route)
                },
                onNavigateToTimer = {
                    navController.navigate(Routes.Timer.route)
                },
                onNavigateToStopwatch = {
                    navController.navigate(Routes.Stopwatch.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                },
                onNavigateToCreateAlarm = {
                    navController.navigate(Routes.AlarmEdit.createRoute(0L))
                },
                onNavigateToEditAlarm = { alarmId ->
                    navController.navigate(Routes.AlarmEdit.createRoute(alarmId))
                },
                onNavigateToCreateTimer = {
                    navController.navigate(Routes.Timer.route)
                },
                onNavigateToEditTimer = { timerId ->
                    // Timer doesn't have separate edit screen, go to timer screen
                    navController.navigate(Routes.Timer.route)
                },
                onVoiceCommand = {
                    navController.navigate(Routes.VoiceCommand.route)
                }
            )
        }

        // Alarms list screen
        composable(Routes.Alarms.route) {
            AlarmScreen(
                onNavigateToEdit = { alarmId ->
                    navController.navigate(Routes.AlarmEdit.createRoute(alarmId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Alarm edit/create screen
        composable(
            route = Routes.AlarmEdit.route,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            AlarmEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Timer screen
        composable(Routes.Timer.route) {
            TimerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Stopwatch screen
        composable(Routes.Stopwatch.route) {
            StopwatchScreen(
                modifier = Modifier
            )
        }

        // World Clocks screen
        composable(Routes.WorldClocks.route) {
            WorldClocksScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings screen
        composable(Routes.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Voice Command screen
        composable(Routes.VoiceCommand.route) {
            VoiceCommandScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAlarmEdit = { alarmId ->
                    navController.navigate(Routes.AlarmEdit.createRoute(alarmId))
                },
                onNavigateToTimer = {
                    navController.navigate(Routes.Timer.route)
                }
            )
        }
    }
}
