package com.voicebell.clock.presentation.screens.timer

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.domain.model.Timer
import kotlinx.coroutines.delay

/**
 * Timer screen with input, active timer display, and recent timers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onNavigateToSettings: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Force recomposition every second when timer is running
    val currentTime by produceState(initialValue = System.currentTimeMillis(), state.activeTimer) {
        while (state.activeTimer?.isRunning == true && state.activeTimer?.isPaused == false) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    // Show error messages
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(TimerEvent.DismissError)
        }
    }

    Scaffold(
        topBar = if (showTopBar) {
            {
                TopAppBar(
                    title = {
                        Text(
                            text = "Timer",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        } else {
            {}
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active timer display
            if (state.activeTimer != null) {
                item {
                    ActiveTimerCard(
                        timer = state.activeTimer!!,
                        onPause = { viewModel.onEvent(TimerEvent.PauseTimer) },
                        onResume = { viewModel.onEvent(TimerEvent.ResumeTimer) },
                        onStop = { viewModel.onEvent(TimerEvent.StopTimer) }
                    )
                }
            }

            // Timer input (only when no active timer)
            if (state.activeTimer == null) {
                item {
                    TimerInputCard(
                        hours = state.inputHours,
                        minutes = state.inputMinutes,
                        seconds = state.inputSeconds,
                        label = state.inputLabel,
                        vibrateEnabled = state.vibrateEnabled,
                        canStart = state.canStart,
                        isLoading = state.isLoading,
                        onHoursChange = { viewModel.onEvent(TimerEvent.SetHours(it)) },
                        onMinutesChange = { viewModel.onEvent(TimerEvent.SetMinutes(it)) },
                        onSecondsChange = { viewModel.onEvent(TimerEvent.SetSeconds(it)) },
                        onLabelChange = { viewModel.onEvent(TimerEvent.SetLabel(it)) },
                        onVibrateToggle = { viewModel.onEvent(TimerEvent.ToggleVibrate(it)) },
                        onStart = { viewModel.onEvent(TimerEvent.StartTimer) }
                    )
                }
            }

            // Recent timers
            if (state.hasRecentTimers) {
                item {
                    Text(
                        text = "Recent Timers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.timers.take(5)) { timer ->
                    RecentTimerCard(
                        timer = timer,
                        onRestart = { viewModel.onEvent(TimerEvent.RestartTimer(timer)) },
                        onDelete = { viewModel.onEvent(TimerEvent.DeleteTimer(timer.id)) }
                    )
                }
            }
        }
    }
}

/**
 * Active timer display with progress and controls
 */
@Composable
private fun ActiveTimerCard(
    timer: Timer,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Label
            if (timer.label.isNotBlank()) {
                Text(
                    text = timer.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Time display
            Text(
                text = timer.getFormattedTime(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Progress indicator
            LinearProgressIndicator(
                progress = timer.getProgress(),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stop button
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop")
                }

                // Pause/Resume button
                Button(
                    onClick = if (timer.isPaused) onResume else onPause,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (timer.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (timer.isPaused) "Resume" else "Pause")
                }
            }
        }
    }
}

/**
 * Timer input card with time fields and start button
 */
@Composable
private fun TimerInputCard(
    hours: Int,
    minutes: Int,
    seconds: Int,
    label: String,
    vibrateEnabled: Boolean,
    canStart: Boolean,
    isLoading: Boolean,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onLabelChange: (String) -> Unit,
    onVibrateToggle: (Boolean) -> Unit,
    onStart: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hours
                TimeInputField(
                    value = hours,
                    onValueChange = onHoursChange,
                    label = "Hours",
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Minutes
                TimeInputField(
                    value = minutes,
                    onValueChange = onMinutesChange,
                    label = "Min",
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Seconds
                TimeInputField(
                    value = seconds,
                    onValueChange = onSecondsChange,
                    label = "Sec",
                    modifier = Modifier.weight(1f)
                )
            }

            // Label input
            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                label = { Text("Label (optional)") },
                placeholder = { Text("Cooking timer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Label, contentDescription = null)
                }
            )

            // Vibrate toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null)
                    Text("Vibrate when finished")
                }
                Switch(
                    checked = vibrateEnabled,
                    onCheckedChange = onVibrateToggle
                )
            }

            // Start button
            Button(
                onClick = onStart,
                enabled = canStart && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Timer", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Individual time input field (hours/minutes/seconds)
 */
@Composable
private fun TimeInputField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { text ->
            val newValue = text.filter { it.isDigit() }.toIntOrNull() ?: 0
            onValueChange(newValue)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
    )
}

/**
 * Recent timer card with restart and delete buttons
 */
@Composable
private fun RecentTimerCard(
    timer: Timer,
    onRestart: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (timer.isFinished) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Show original duration for finished timers, current remaining for active timers
                val displayTime = if (timer.isFinished) {
                    formatDuration(timer.durationMillis)
                } else {
                    timer.getFormattedTime()
                }
                Text(
                    text = displayTime,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (timer.label.isNotBlank()) {
                    Text(
                        text = timer.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (timer.isFinished) {
                    Text(
                        text = "Finished",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Restart button
                IconButton(onClick = onRestart) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Restart",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Format duration in milliseconds to HH:MM:SS or MM:SS
 */
private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 60000) % 60
    val hours = millis / 3600000

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
