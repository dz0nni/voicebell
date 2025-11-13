package com.voicebell.clock.presentation.screens.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.presentation.screens.voice.VoiceCommandEvent
import com.voicebell.clock.presentation.screens.voice.VoiceCommandViewModel
import com.voicebell.clock.service.VoiceRecognitionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main home screen with all features on single screen:
 * - Recent alarms at top (enable/disable toggles)
 * - Recent timers below (restart buttons)
 * - Large voice command button at bottom
 * - Stopwatch card at top (toggleable)
 * - Expandable FAB with quick add alarm/timer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalHomeScreen(
    recentAlarms: List<Alarm>,
    recentTimers: List<Timer>,
    use24HourFormat: Boolean,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onEditAlarm: (Long) -> Unit,
    onDeleteAlarm: (Long) -> Unit,
    onRestartTimer: (Long) -> Unit,
    onStopTimer: (Long) -> Unit,
    onDeleteTimer: (Long) -> Unit,
    onEditTimer: (Long) -> Unit,
    onCreateAlarm: () -> Unit,
    onCreateTimer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onVoiceCommand: () -> Unit,
    modifier: Modifier = Modifier,
    voiceViewModel: VoiceCommandViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val voiceState by voiceViewModel.state.collectAsStateWithLifecycle()
    var isFabExpanded by remember { mutableStateOf(false) }
    var isStopwatchVisible by remember { mutableStateOf(false) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            voiceViewModel.onEvent(VoiceCommandEvent.ShowError("Microphone permission is required"))
        }
    }

    // Register broadcast receiver for voice recognition results
    // Note: Duplicate execution is prevented by ViewModel's deduplication logic
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val text = intent?.getStringExtra(VoiceRecognitionService.EXTRA_RESULT_TEXT)
                val success = intent?.getBooleanExtra(VoiceRecognitionService.EXTRA_RESULT_SUCCESS, false) ?: false

                if (success && text != null) {
                    voiceViewModel.onEvent(VoiceCommandEvent.RecognitionResult(text))
                } else {
                    voiceViewModel.onEvent(VoiceCommandEvent.ShowError(text ?: "Recognition failed"))
                }
            }
        }

        val filter = IntentFilter(VoiceRecognitionService.ACTION_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VoiceBell",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Voice Command Debug button (stats for geeks)
                    IconButton(onClick = onVoiceCommand) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Voice Debug (stats for geeks)",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Stopwatch toggle button
                    IconButton(onClick = { isStopwatchVisible = !isStopwatchVisible }) {
                        if (isStopwatchVisible) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                tonalElevation = 4.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Toggle Stopwatch",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Toggle Stopwatch",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
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
        },
        bottomBar = {
            VoiceCommandBottomBar(
                isListening = voiceState.isListening,
                hasPermission = hasAudioPermission,
                onStartListening = {
                    if (hasAudioPermission) {
                        voiceViewModel.onEvent(VoiceCommandEvent.StartListening)
                        VoiceRecognitionService.startListening(context)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopListening = {
                    voiceViewModel.onEvent(VoiceCommandEvent.StopListening)
                    VoiceRecognitionService.stopListening(context)
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
            // Integrated Stopwatch Card (shown when toggled with slide animation)
            item {
                AnimatedVisibility(
                    visible = isStopwatchVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it }, // Slide from top (negative offset)
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it }, // Slide to top (negative offset)
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeOut()
                ) {
                    IntegratedStopwatchCard(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Empty state when no alarms or timers
            if (recentAlarms.isEmpty() && recentTimers.isEmpty()) {
                item {
                    EmptyStateView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    )
                }
            }

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
                        use24HourFormat = use24HourFormat,
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

            // Bottom spacing for FAB and bottom bar
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
    use24HourFormat: Boolean,
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
                    text = alarm.getFormattedTime(use24HourFormat),
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
            .height(120.dp)
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
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            // Stopwatch/Pause button
            IconButton(
                onClick = { isRunning = !isRunning }
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
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

/**
 * Empty state view shown when no alarms or timers exist
 */
@Composable
private fun EmptyStateView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large microphone icon
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title text
        Text(
            text = "Use Voice Commands",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle text
        Text(
            text = "Press the microphone button at the bottom and try:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Examples
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "• \"Set alarm for 7 in the morning\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "• \"Set timer 5 minutes\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Bottom bar with voice command button (sticky)
 */
@Composable
private fun VoiceCommandBottomBar(
    isListening: Boolean,
    hasPermission: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            PushToTalkButton(
                isListening = isListening,
                enabled = hasPermission,
                onStartListening = onStartListening,
                onStopListening = onStopListening
            )
        }
    }
}

/**
 * Push-to-talk button for voice recognition
 */
@Composable
private fun PushToTalkButton(
    isListening: Boolean,
    enabled: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var isHoldingMode by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf(0L) }
    var isProcessing by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isListening) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing background when listening
        if (isListening) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.3f)
            ) {}
        }

        // Main button
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (isProcessing) return@detectTapGestures

                            pressStartTime = System.currentTimeMillis()

                            if (!isListening) {
                                isProcessing = true
                                isHoldingMode = true
                                scope.launch {
                                    onStartListening()
                                    delay(500)
                                    isProcessing = false
                                }
                            }

                            // Wait for release
                            tryAwaitRelease()

                            // Handle release
                            val pressDuration = System.currentTimeMillis() - pressStartTime

                            if (isHoldingMode && pressDuration < 300 && !isListening) {
                                // Quick tap - do nothing, already stopped
                            } else if (isListening) {
                                // Stop listening on release
                                scope.launch {
                                    onStopListening()
                                    delay(300)
                                    isHoldingMode = false
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                tonalElevation = if (isListening) 16.dp else 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Command",
                        modifier = Modifier.size(60.dp),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
