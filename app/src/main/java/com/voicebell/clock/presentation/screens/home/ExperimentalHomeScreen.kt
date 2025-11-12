package com.voicebell.clock.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.Timer

/**
 * Experimental home screen with all features on single screen:
 * - Recent alarms at top (enable/disable toggles)
 * - Recent timers below (restart buttons)
 * - Large voice command button in center
 * - Stopwatch quick launch at bottom
 * - Expandable FAB with quick add alarm/timer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalHomeScreen(
    recentAlarms: List<Alarm>,
    recentTimers: List<Timer>,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onEditAlarm: (Long) -> Unit,
    onDeleteAlarm: (Long) -> Unit,
    onRestartTimer: (Long) -> Unit,
    onStopTimer: (Long) -> Unit,
    onDeleteTimer: (Long) -> Unit,
    onEditTimer: (Long) -> Unit,
    onVoiceCommand: () -> Unit,
    onCreateAlarm: () -> Unit,
    onCreateTimer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "VoiceBell",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Experimental View",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Voice Command button
                    IconButton(onClick = onVoiceCommand) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            tonalElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Command",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    // Settings button
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
        floatingActionButton = {
            ExpandableFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onCreateAlarm = {
                    onCreateAlarm()
                    isFabExpanded = false
                },
                onCreateTimer = {
                    onCreateTimer()
                    isFabExpanded = false
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Recent Alarms Section
            if (recentAlarms.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recent Alarms",
                        icon = Icons.Default.Alarm
                    )
                }

                items(recentAlarms) { alarm ->
                    CompactAlarmCard(
                        alarm = alarm,
                        onToggle = onToggleAlarm,
                        onDelete = { onDeleteAlarm(alarm.id) },
                        onClick = { onEditAlarm(alarm.id) }
                    )
                }
            }

            // Recent Timers Section
            if (recentTimers.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recent Timers",
                        icon = Icons.Default.Timer
                    )
                }

                items(recentTimers) { timer ->
                    CompactTimerCard(
                        timer = timer,
                        onRestart = { onRestartTimer(timer.id) },
                        onStop = { onStopTimer(timer.id) },
                        onDelete = { onDeleteTimer(timer.id) },
                        onClick = { onEditTimer(timer.id) }
                    )
                }
            }

            // Integrated Stopwatch Card
            item {
                IntegratedStopwatchCard(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * Section header with icon and title
 */
@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Compact alarm card for recent alarms
 */
@Composable
private fun CompactAlarmCard(
    alarm: Alarm,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Alarm?") },
            text = { Text("This will permanently delete this alarm.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showDeleteDialog = true
                        }
                    )
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.getFormattedTime(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.clickable { onClick() }
                )
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle(alarm.id, it) }
            )
        }
    }
}

/**
 * Compact timer card for recent timers with real-time countdown
 */
@Composable
private fun CompactTimerCard(
    timer: Timer,
    onRestart: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    // Real-time countdown update
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(timer.id, timer.isRunning) {
        if (timer.isRunning && !timer.isPaused) {
            while (true) {
                currentTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(100)
            }
        }
    }

    val remainingMillis = if (timer.isRunning && !timer.isPaused) {
        (timer.endTime - currentTime).coerceAtLeast(0)
    } else {
        timer.remainingMillis
    }

    val isTimerActive = timer.isRunning && !timer.isPaused
    val borderColor = MaterialTheme.colorScheme.primary

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Timer?") },
            text = { Text("This will permanently delete this timer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isTimerActive) 3.dp else 0.dp,
            color = if (isTimerActive) borderColor else Color.Transparent
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isTimerActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showDeleteDialog = true
                        }
                    )
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Show original duration if not active, otherwise show remaining time
                val displayTime = if (isTimerActive) {
                    formatMillisToTime(remainingMillis)
                } else {
                    formatMillisToTime(timer.durationMillis)
                }

                Text(
                    text = displayTime,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isTimerActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (timer.label.isNotBlank()) {
                    Text(
                        text = timer.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Show Stop button when timer is active, Restart button when inactive
            IconButton(onClick = if (isTimerActive) onStop else onRestart) {
                Icon(
                    imageVector = if (isTimerActive) Icons.Default.Stop else Icons.Default.Replay,
                    contentDescription = if (isTimerActive) "Stop timer" else "Restart timer",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Large prominent voice command button
 */
@Composable
private fun LargeVoiceCommandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isPressed) 0.95f else 1f),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = "Voice Command",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Tap to set alarm or timer by voice",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * Integrated stopwatch card with play/pause and reset functionality
 */
@Composable
private fun IntegratedStopwatchCard(
    modifier: Modifier = Modifier
) {
    // Stopwatch state
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Update elapsed time when running
    LaunchedEffect(isRunning) {
        if (isRunning) {
            startTime = System.currentTimeMillis() - elapsedMillis
            while (isRunning) {
                elapsedMillis = System.currentTimeMillis() - startTime
                kotlinx.coroutines.delay(10) // Update every 10ms for smooth display
            }
        }
    }

    // Animate card height - stays large when paused, only resets to small on reset
    val cardHeight by animateDpAsState(
        targetValue = if (isRunning || elapsedMillis > 0) 120.dp else 80.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardHeight"
    )

    // Format time as 00:00.000
    val formattedTime = remember(elapsedMillis) {
        val totalSeconds = elapsedMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = elapsedMillis % 1000
        String.format("%02d:%02d.%03d", minutes, seconds, millis)
    }

    // Reset dialog
    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Stopwatch?") },
            text = { Text("This will reset the stopwatch to 00:00.000") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isRunning = false
                        elapsedMillis = 0L
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .height(cardHeight)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (!isRunning && elapsedMillis > 0) {
                            showResetDialog = true
                        }
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (elapsedMillis > 0) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time display
            Text(
                text = formattedTime,
                style = if (isRunning || elapsedMillis > 0) {
                    MaterialTheme.typography.displayMedium
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold,
                color = if (isRunning) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else if (elapsedMillis > 0) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // Stopwatch/Pause button
            IconButton(
                onClick = { isRunning = !isRunning }
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.Timer,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = if (isRunning) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else if (elapsedMillis > 0) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Expandable FAB with quick add options
 */
@Composable
private fun ExpandableFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCreateAlarm: () -> Unit,
    onCreateTimer: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mini FABs (visible when expanded)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "New Timer",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onCreateTimer,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Create timer"
                        )
                    }
                }

                // Add Alarm
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "New Alarm",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onCreateAlarm,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Create alarm"
                        )
                    }
                }
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isExpanded) "Close menu" else "Open menu"
            )
        }
    }
}

/**
 * Sticky voice command bar at bottom
 */
@Composable
private fun StickyVoiceCommandBar(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Text(
                text = "Tap to set alarm or timer by voice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Format milliseconds to duration string
 */
private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Format milliseconds to time string (MM:SS or HH:MM:SS)
 * Rounds up to the next second for display (e.g., 4:59.1 shows as 5:00)
 */
private fun formatMillisToTime(millis: Long): String {
    // Round up to next second by adding 999ms before division
    val totalSeconds = ((millis + 999) / 1000).toInt()
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
