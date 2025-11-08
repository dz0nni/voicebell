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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
                val text = intent?.getStringExtra(VoiceRecognitionService.EXTRA_RESULT_TEXT)
                val success = intent?.getBooleanExtra(VoiceRecognitionService.EXTRA_RESULT_SUCCESS, false) ?: false

                if (success && text != null) {
                    viewModel.onEvent(VoiceCommandEvent.RecognitionResult(text))
                } else {
                    viewModel.onEvent(VoiceCommandEvent.ShowError(text ?: "Recognition failed"))
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
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Instruction text
            Text(
                text = if (state.isListening) {
                    "Listening... Speak now"
                } else {
                    "Tap to start listening"
                },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Microphone button with animation
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
                }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Result text
            AnimatedVisibility(
                visible = state.resultText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Microphone permission is required for voice commands",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            // Examples
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Try saying:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• \"Set alarm for 7 AM\"\n• \"Set timer for 5 minutes\"\n• \"Wake me up at 6:30\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
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
