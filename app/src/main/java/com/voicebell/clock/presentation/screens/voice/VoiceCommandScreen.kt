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
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
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
import com.voicebell.clock.service.VoiceRecognitionService

/**
 * Voice Command screen for setting alarms/timers by voice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAlarmEdit: (Long) -> Unit,
    onNavigateToTimer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoiceCommandViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onEvent(VoiceCommandEvent.StartListening)
            } else {
                viewModel.onEvent(VoiceCommandEvent.PermissionDenied)
            }
        }
    )

    // Check and request permission
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onEvent(VoiceCommandEvent.StartListening)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Register broadcast receiver for recognition results
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    VoiceRecognitionService.BROADCAST_RECOGNITION_RESULT -> {
                        val text = intent.getStringExtra(VoiceRecognitionService.EXTRA_RESULT_TEXT) ?: ""
                        viewModel.onEvent(VoiceCommandEvent.RecognitionResult(text))
                    }
                    VoiceRecognitionService.BROADCAST_RECOGNITION_ERROR -> {
                        val error = intent.getStringExtra(VoiceRecognitionService.EXTRA_RESULT_TEXT) ?: "Unknown error"
                        viewModel.onEvent(VoiceCommandEvent.RecognitionError(error))
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(VoiceRecognitionService.BROADCAST_RECOGNITION_RESULT)
            addAction(VoiceRecognitionService.BROADCAST_RECOGNITION_ERROR)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VoiceCommandEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is VoiceCommandEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Long
                    )
                }
                is VoiceCommandEffect.NavigateToAlarmEdit -> {
                    onNavigateToAlarmEdit(0L) // Create new alarm
                    onNavigateBack()
                }
                is VoiceCommandEffect.NavigateToTimer -> {
                    onNavigateToTimer()
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Command") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(VoiceCommandEvent.StopListening)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                state.isListening -> {
                    ListeningState(
                        recognizedText = state.recognizedText
                    )
                }
                state.isParsing -> {
                    ParsingState()
                }
                state.permissionDenied -> {
                    PermissionDeniedState()
                }
                else -> {
                    IdleState()
                }
            }
        }
    }
}

/**
 * Listening state with animated microphone icon.
 */
@Composable
private fun ListeningState(
    recognizedText: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Animated microphone icon
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Listening...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (recognizedText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = recognizedText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = "Try saying:\n\"Set alarm for 7 AM\"\n\"Timer for 5 minutes\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Parsing state.
 */
@Composable
private fun ParsingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "Processing command...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Permission denied state.
 */
@Composable
private fun PermissionDeniedState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = "Microphone Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Please grant microphone permission to use voice commands",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Idle state (should not be shown normally).
 */
@Composable
private fun IdleState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()

        Text(
            text = "Initializing...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
