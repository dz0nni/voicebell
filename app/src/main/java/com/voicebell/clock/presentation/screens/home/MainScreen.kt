package com.voicebell.clock.presentation.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.domain.model.UiMode

/**
 * Main screen that switches between Classic and Experimental layouts
 * based on user settings.
 */
@Composable
fun MainScreen(
    onNavigateToAlarms: () -> Unit,
    onNavigateToWorldClocks: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToStopwatch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateAlarm: () -> Unit,
    onNavigateToEditAlarm: (Long) -> Unit,
    onNavigateToCreateTimer: () -> Unit,
    onNavigateToEditTimer: (Long) -> Unit,
    onVoiceCommand: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state.settings.uiMode) {
        UiMode.CLASSIC -> {
            ClassicHomeScreen(
                onNavigateToAlarms = onNavigateToAlarms,
                onNavigateToWorldClocks = onNavigateToWorldClocks,
                onNavigateToTimer = onNavigateToTimer,
                onNavigateToStopwatch = onNavigateToStopwatch,
                onNavigateToSettings = onNavigateToSettings,
                modifier = modifier
            )
        }
        UiMode.EXPERIMENTAL -> {
            ExperimentalHomeScreen(
                recentAlarms = state.recentAlarms,
                recentTimers = state.recentTimers,
                onToggleAlarm = { alarmId, isEnabled ->
                    // TODO: Handle alarm toggle
                },
                onEditAlarm = onNavigateToEditAlarm,
                onRestartTimer = { timerId ->
                    // TODO: Handle timer restart
                },
                onEditTimer = onNavigateToEditTimer,
                onVoiceCommand = onVoiceCommand,
                onStartStopwatch = onNavigateToStopwatch,
                onCreateAlarm = onNavigateToCreateAlarm,
                onCreateTimer = onNavigateToCreateTimer,
                onNavigateToSettings = onNavigateToSettings,
                modifier = modifier
            )
        }
    }
}
