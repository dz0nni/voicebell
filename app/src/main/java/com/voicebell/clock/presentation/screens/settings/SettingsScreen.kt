package com.voicebell.clock.presentation.screens.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.util.PermissionsHelper
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import com.voicebell.clock.BuildConfig

/**
 * Settings screen for app preferences including UI mode switcher.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showUiModeDialog by remember { mutableStateOf(false) }

    // Get PermissionsHelper from context
    val context = LocalContext.current
    val permissionsHelper = remember {
        PermissionsHelper(context)
    }

    // Check permissions on each recomposition to show current status
    var permissionStatus by remember { mutableStateOf(permissionsHelper.getPermissionStatus()) }

    // Refresh permission status when user returns to the app (e.g., from system settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // User returned to the app - refresh permission status
                permissionStatus = permissionsHelper.getPermissionStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launchers for runtime permissions
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = permissionsHelper.getPermissionStatus()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = permissionsHelper.getPermissionStatus()
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = permissionsHelper.getPermissionStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // UI Mode Section
            item {
                SettingsSectionHeader(title = "Appearance")
            }

            item {
                SettingsItem(
                    title = "UI Mode",
                    subtitle = state.settings.uiMode.displayName,
                    onClick = { showUiModeDialog = true }
                )
            }

            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Voice Commands Section
            item {
                SettingsSectionHeader(title = "Voice Commands")
            }

            // Voice model download button
            item {
                VoiceModelDownloadButton(
                    isModelDownloaded = state.isVoiceModelDownloaded,
                    isDownloading = state.isDownloadingModel,
                    downloadProgress = state.modelDownloadProgress,
                    onDownload = { viewModel.downloadVoiceModel() },
                    onDelete = { viewModel.deleteVoiceModel() }
                )
            }

            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Display Section
            item {
                SettingsSectionHeader(title = "Display")
            }

            item {
                SettingsSwitchItem(
                    title = "24-hour format",
                    subtitle = "Use 24-hour time format",
                    checked = state.settings.use24HourFormat,
                    onCheckedChange = { viewModel.toggle24HourFormat() }
                )
            }

            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Timer Section
            item {
                SettingsSectionHeader(title = "Timer")
            }

            item {
                SettingsSwitchItem(
                    title = "Auto-delete finished timers",
                    subtitle = "Remove completed timers when starting a new one",
                    checked = state.settings.autoDeleteFinishedTimer,
                    onCheckedChange = { viewModel.toggleAutoDeleteFinishedTimer() }
                )
            }

            item {
                SettingsSwitchItem(
                    title = "Bluetooth headphones only",
                    subtitle = "Play timer alerts only to Bluetooth headphones when connected",
                    checked = state.settings.playTimerSoundOnlyToBluetooth,
                    onCheckedChange = { viewModel.togglePlayTimerSoundOnlyToBluetooth() }
                )
            }

            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Permissions Section
            item {
                SettingsSectionHeader(title = "Permissions")
            }

            // Warning card if not all permissions granted
            if (!permissionStatus.criticalGranted) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Critical permissions missing. Voice commands and alarms require all permissions below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // 1. Microphone permission (CRITICAL - core feature)
            item {
                PermissionItem(
                    title = "Microphone Access",
                    subtitle = "Critical: Required for voice commands (main feature)",
                    isGranted = permissionStatus.recordAudioGranted,
                    onClick = {
                        if (!permissionStatus.recordAudioGranted) {
                            microphonePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    buttonText = if (permissionStatus.recordAudioGranted) null else "Grant"
                )
            }

            // 2. Notifications permission (CRITICAL)
            item {
                PermissionItem(
                    title = "Notifications",
                    subtitle = "Critical: Required for all alarm and timer alerts",
                    isGranted = permissionStatus.notificationsEnabled,
                    onClick = {
                        if (!permissionStatus.notificationsEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                permissionsHelper.openNotificationSettings()
                            }
                        }
                    },
                    buttonText = if (permissionStatus.notificationsEnabled) null else "Grant"
                )
            }

            // 3. Exact alarms permission (CRITICAL)
            item {
                PermissionItem(
                    title = "Schedule Exact Alarms",
                    subtitle = "Critical: Required for precise alarm timing",
                    isGranted = permissionStatus.canScheduleExactAlarms,
                    onClick = { permissionsHelper.openAlarmSettings() },
                    buttonText = if (permissionStatus.canScheduleExactAlarms) null else "Configure"
                )
            }

            // 4. Battery optimization (IMPORTANT for reliability)
            item {
                PermissionItem(
                    title = "Battery Optimization",
                    subtitle = "Important: Disable for reliable background operation",
                    isGranted = permissionStatus.batteryOptimizationDisabled,
                    onClick = {
                        if (!permissionStatus.batteryOptimizationDisabled) {
                            permissionsHelper.requestIgnoreBatteryOptimization()
                        }
                    },
                    buttonText = if (permissionStatus.batteryOptimizationDisabled) null else "Grant"
                )
            }

            // 5. Full-screen intent permission (RECOMMENDED)
            item {
                PermissionItem(
                    title = "Full-Screen Notifications",
                    subtitle = "Recommended: Show alarms when phone is locked",
                    isGranted = permissionStatus.canUseFullScreenIntent,
                    onClick = { permissionsHelper.openFullScreenIntentSettings() },
                    buttonText = if (permissionStatus.canUseFullScreenIntent) null else "Configure"
                )
            }

            // 6. Bluetooth permission (OPTIONAL - for headset-only timer alerts)
            item {
                PermissionItem(
                    title = "Bluetooth Access",
                    subtitle = "Optional: Detect Bluetooth headsets for audio routing",
                    isGranted = permissionStatus.bluetoothConnectGranted,
                    onClick = {
                        if (!permissionStatus.bluetoothConnectGranted) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                bluetoothPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
                            }
                        }
                    },
                    buttonText = if (permissionStatus.bluetoothConnectGranted) null else "Grant"
                )
            }

            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Main Screen Settings
            if (state.settings.uiMode == UiMode.EXPERIMENTAL) {
                item {
                    SettingsSectionHeader(title = "Main Screen")
                }

                item {
                    SettingsItem(
                        title = "Show recent alarms",
                        subtitle = "Display up to ${state.settings.maxRecentAlarms} alarms",
                        onClick = { /* TODO: Show number picker */ }
                    )
                }

                item {
                    SettingsItem(
                        title = "Show recent timers",
                        subtitle = "Display up to ${state.settings.maxRecentTimers} timers",
                        onClick = { /* TODO: Show number picker */ }
                    )
                }

                item {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            // About Section
            item {
                SettingsSectionHeader(title = "About")
            }

            item {
                SettingsItem(
                    title = "Version",
                    subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = "Open source licenses",
                    subtitle = "View licenses",
                    onClick = { /* TODO: Navigate to licenses */ }
                )
            }
        }
    }

    // UI Mode Dialog
    if (showUiModeDialog) {
        UiModeDialog(
            currentMode = state.settings.uiMode,
            onDismiss = { showUiModeDialog = false },
            onSelectMode = { mode ->
                viewModel.setUiMode(mode)
                showUiModeDialog = false
            }
        )
    }
}

/**
 * Settings section header
 */
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/**
 * Clickable settings item
 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * Settings item with switch
 */
@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

/**
 * Permission item showing status and action button
 */
@Composable
private fun PermissionItem(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    buttonText: String? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (isGranted) "Granted" else "Not granted",
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        trailingContent = {
            if (!isGranted && buttonText != null) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(buttonText)
                }
            }
        },
        modifier = Modifier.clickable(enabled = !isGranted, onClick = onClick)
    )
}

/**
 * UI Mode selection dialog
 */
@Composable
private fun UiModeDialog(
    currentMode: UiMode,
    onDismiss: () -> Unit,
    onSelectMode: (UiMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select UI Mode")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UiMode.values().forEach { mode ->
                    val isDisabled = mode == UiMode.CLASSIC
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (!isDisabled) {
                                    Modifier.clickable { onSelectMode(mode) }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(vertical = 12.dp)
                            .alpha(if (isDisabled) 0.4f else 1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (mode == currentMode) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = when (mode) {
                                    UiMode.EXPERIMENTAL -> "All features on one screen with voice button"
                                    UiMode.CLASSIC -> "Traditional layout with tabs (temporarily disabled)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (mode == currentMode) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Voice model download button
 */
@Composable
private fun VoiceModelDownloadButton(
    isModelDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isModelDownloaded) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice Recognition Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isModelDownloaded) {
                            "Model installed (~40 MB)"
                        } else {
                            "Setup required (~40 MB)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isDownloading) {
                    CircularProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    if (isModelDownloaded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Model ready",
                            tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Button(onClick = onDownload) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Setup")
                        }
                    }
                }
            }

            // Progress indicator
            if (isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = downloadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Info text
            if (!isModelDownloaded && !isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Voice commands require offline speech recognition model. Download over WiFi recommended.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
