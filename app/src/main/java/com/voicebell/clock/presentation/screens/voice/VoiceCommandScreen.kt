package com.voicebell.clock.presentation.screens.voice

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.service.VoiceRecognitionService

/**
 * Voice command screen with push-to-talk button.
 *
 * Features:
 * - Large microphone button (press and hold to speak)
 * - Real-time feedback
 * - Permission handling
 * - Result display
 * - State persists across navigation (ViewModel scoped to NavGraph)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandScreen(
    viewModel: VoiceCommandViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            viewModel.onEvent(VoiceCommandEvent.ShowError("Microphone permission is required"))
        }
    }

    // Register broadcast receiver for voice recognition results
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                android.util.Log.d("VoiceCommandScreen", "Broadcast received! Action: ${intent?.action}")
                val text = intent?.getStringExtra(VoiceRecognitionService.EXTRA_RESULT_TEXT)
                val success = intent?.getBooleanExtra(VoiceRecognitionService.EXTRA_RESULT_SUCCESS, false) ?: false
                android.util.Log.d("VoiceCommandScreen", "Text: $text, Success: $success")

                if (success && text != null) {
                    android.util.Log.d("VoiceCommandScreen", "Sending RecognitionResult event to ViewModel")
                    viewModel.onEvent(VoiceCommandEvent.RecognitionResult(text))
                } else {
                    android.util.Log.d("VoiceCommandScreen", "Sending ShowError event to ViewModel")
                    viewModel.onEvent(VoiceCommandEvent.ShowError(text ?: "Recognition failed"))
                }
            }
        }

        val filter = IntentFilter(VoiceRecognitionService.ACTION_RESULT)
        android.util.Log.d("VoiceCommandScreen", "Registering receiver with filter: ${VoiceRecognitionService.ACTION_RESULT}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        android.util.Log.d("VoiceCommandScreen", "Receiver registered successfully")

        onDispose {
            android.util.Log.d("VoiceCommandScreen", "Unregistering receiver")
            context.unregisterReceiver(receiver)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Command") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Log area (scrollable, takes remaining space)
            ProcessLogArea(
                logs = state.logMessages,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Result text with timer countdown (below logs)
            AnimatedVisibility(
                visible = state.resultText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isSuccess) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
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
                        Text(
                            text = state.resultText,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.isSuccess) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )

                        // Show timer countdown if active timer exists
                        state.activeTimer?.let { timer ->
                            // Real-time countdown display
                            var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

                            // Update every second
                            LaunchedEffect(timer.id) {
                                while (true) {
                                    currentTime = System.currentTimeMillis()
                                    delay(100) // Update 10 times per second for smooth display
                                }
                            }

                            // Calculate remaining time based on current time
                            val remainingMillis = if (timer.isRunning && !timer.isPaused) {
                                (timer.endTime - currentTime).coerceAtLeast(0)
                            } else {
                                timer.remainingMillis
                            }

                            Text(
                                text = formatTimerRemaining(remainingMillis),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isSuccess) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                        }
                    }
                }
            }

            // Permission prompt
            if (!hasAudioPermission) {
                Text(
                    text = "Microphone permission is required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Microphone button (centered, no extra text)
            PushToTalkButton(
                isListening = state.isListening,
                enabled = hasAudioPermission,
                onStartListening = {
                    if (hasAudioPermission) {
                        viewModel.onEvent(VoiceCommandEvent.StartListening)
                        VoiceRecognitionService.startListening(context)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopListening = {
                    viewModel.onEvent(VoiceCommandEvent.StopListening)
                    VoiceRecognitionService.stopListening(context)
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Examples (compact, at very bottom)
            Text(
                text = "Try: \"Set alarm for 7 AM\" or \"Set timer for 5 minutes\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Process log area showing recognition and parsing steps.
 */
@Composable
private fun ProcessLogArea(
    logs: List<LogMessage>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to top when new logs arrive (newest first)
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        if (logs.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Process log",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Voice recognition steps will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Log messages (newest first)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs.reversed()) { log ->
                    LogMessageItem(log)
                }
            }
        }
    }
}

/**
 * Single log message item.
 */
@Composable
private fun LogMessageItem(
    log: LogMessage,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (log.type) {
        LogType.INFO -> MaterialTheme.colorScheme.surfaceVariant
        LogType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        LogType.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        LogType.PROCESSING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
    }

    val textColor = when (log.type) {
        LogType.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
        LogType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        LogType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        LogType.PROCESSING -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Timestamp
            Text(
                text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(end = 8.dp)
            )

            // Message
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PushToTalkButton(
    isListening: Boolean,
    enabled: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Track if user is holding the button (long press mode)
    var isHoldingMode by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf(0L) }
    var isProcessing by remember { mutableStateOf(false) } // Prevent rapid re-triggering

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

        // Main button with dual-mode support
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .pointerInput(enabled) {
                    detectTapGestures(
                        onPress = {
                            if (!enabled || isProcessing) return@detectTapGestures

                            // Record press start time
                            pressStartTime = System.currentTimeMillis()

                            // If not currently listening, start immediately (hold mode)
                            if (!isListening) {
                                isProcessing = true
                                isHoldingMode = true
                                scope.launch {
                                    onStartListening()
                                    delay(500) // Debounce: prevent rapid re-triggering
                                    isProcessing = false
                                }
                            }

                            // Wait for release
                            val released = tryAwaitRelease()

                            // Calculate how long the button was pressed
                            val pressDuration = System.currentTimeMillis() - pressStartTime

                            if (released) {
                                if (isHoldingMode && pressDuration < 300) {
                                    // Very short press - treat as toggle mode, don't stop
                                    isHoldingMode = false
                                    // Already started listening, keep it going
                                } else if (isHoldingMode) {
                                    // Long press released - stop listening (hold mode)
                                    isHoldingMode = false
                                    isProcessing = true
                                    scope.launch {
                                        onStopListening()
                                        delay(300)
                                        isProcessing = false
                                    }
                                } else if (!isHoldingMode && isListening) {
                                    // Toggle mode - stop listening
                                    isProcessing = true
                                    scope.launch {
                                        onStopListening()
                                        delay(300)
                                        isProcessing = false
                                    }
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = if (isListening) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop listening" else "Start listening",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Format timer remaining time to display format.
 * Rounds up to the next second for display (e.g., 4:59.1 shows as 5:00)
 * Examples: "1:23" (1 min 23 sec), "45" (45 seconds), "12:34" (12 min 34 sec)
 */
private fun formatTimerRemaining(remainingMillis: Long): String {
    // Round up to next second by adding 999ms before division
    val totalSeconds = ((remainingMillis + 999) / 1000).toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return if (minutes > 0) {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    } else {
        seconds.toString()
    }
}
