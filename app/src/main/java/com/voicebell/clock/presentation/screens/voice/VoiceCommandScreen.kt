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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoiceCommandViewModel = hiltViewModel(),
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

            // Result text (below logs)
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
                    Text(
                        text = state.resultText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = if (state.isSuccess) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
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

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction text (above mic button)
            Text(
                text = if (state.isListening) {
                    "Listening... Speak now"
                } else {
                    "Tap to start"
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone button (fixed at bottom)
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
        FloatingActionButton(
            onClick = {
                if (isListening) {
                    onStopListening()
                } else {
                    onStartListening()
                }
            },
            modifier = Modifier
                .size(150.dp)
                .scale(scale),
            shape = CircleShape,
            containerColor = if (isListening) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
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
