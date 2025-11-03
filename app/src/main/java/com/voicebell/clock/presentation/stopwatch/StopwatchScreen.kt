package com.voicebell.clock.presentation.stopwatch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voicebell.clock.domain.model.Stopwatch

/**
 * Stopwatch screen showing elapsed time, controls, and lap list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(
    modifier: Modifier = Modifier,
    viewModel: StopwatchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StopwatchEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Time display
            TimeDisplay(
                elapsedMillis = state.currentElapsedMillis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Control buttons
            ControlButtons(
                isRunning = state.isRunning,
                isPaused = state.isPaused,
                onStartPause = {
                    if (state.isRunning && !state.isPaused) {
                        viewModel.onEvent(StopwatchEvent.Pause)
                    } else {
                        viewModel.onEvent(StopwatchEvent.Start)
                    }
                },
                onLap = { viewModel.onEvent(StopwatchEvent.AddLap) },
                onReset = { viewModel.onEvent(StopwatchEvent.Reset) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lap list
            if (state.laps.isNotEmpty()) {
                LapList(
                    laps = state.laps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else if (state.isRunning || state.isPaused) {
                // Show hint when stopwatch is running but no laps
                Text(
                    text = "Tap LAP to record a lap time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}

/**
 * Large time display showing elapsed time.
 */
@Composable
private fun TimeDisplay(
    elapsedMillis: Long,
    modifier: Modifier = Modifier
) {
    val formattedTime = formatElapsedTime(elapsedMillis)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Control buttons: Start/Pause, Lap, Reset.
 */
@Composable
private fun ControlButtons(
    isRunning: Boolean,
    isPaused: Boolean,
    onStartPause: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset button (only show if stopwatch has been used)
        if (isRunning || isPaused) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset")
            }
        }

        // Start/Pause button
        FilledTonalButton(
            onClick = onStartPause,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning && !isPaused) "Pause" else "Start",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    !isRunning && !isPaused -> "Start"
                    isPaused -> "Resume"
                    else -> "Pause"
                }
            )
        }

        // Lap button (only enabled when running)
        if (isRunning || isPaused) {
            Button(
                onClick = onLap,
                enabled = isRunning && !isPaused,
                modifier = Modifier.weight(1f)
            ) {
                Text("Lap")
            }
        }
    }
}

/**
 * List of recorded laps.
 */
@Composable
private fun LapList(
    laps: List<Stopwatch.Lap>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to top when new lap is added
    LaunchedEffect(laps.size) {
        if (laps.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Laps",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show laps in reverse order (most recent first)
                items(
                    items = laps.asReversed(),
                    key = { it.lapNumber }
                ) { lap ->
                    LapItem(lap = lap)
                }
            }
        }
    }
}

/**
 * Individual lap item.
 */
@Composable
private fun LapItem(
    lap: Stopwatch.Lap,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap number
            Text(
                text = "Lap ${lap.lapNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Lap time
                Text(
                    text = formatElapsedTime(lap.lapTime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Total time
                Text(
                    text = "Total: ${formatElapsedTime(lap.totalTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Formats elapsed time in milliseconds to HH:MM:SS.mmm format.
 */
private fun formatElapsedTime(millis: Long): String {
    val hours = millis / 3600000
    val minutes = (millis % 3600000) / 60000
    val seconds = (millis % 60000) / 1000
    val milliseconds = millis % 1000

    return if (hours > 0) {
        "%02d:%02d:%02d.%03d".format(hours, minutes, seconds, milliseconds)
    } else {
        "%02d:%02d.%03d".format(minutes, seconds, milliseconds)
    }
}
