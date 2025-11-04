package com.voicebell.clock.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.util.PermissionsHelper
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning

/**
 * Settings screen for app preferences including UI mode switcher.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    permissionsHelper: PermissionsHelper = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showUiModeDialog by remember { mutableStateOf(false) }

    // Check permissions on each recomposition to show current status
    val permissionStatus = remember { permissionsHelper.getPermissionStatus() }

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

            item {
                SettingsSwitchItem(
                    title = "Enable Voice Commands",
                    subtitle = "Use voice to set alarms and timers",
                    checked = state.settings.voiceCommandEnabled,
                    onCheckedChange = { viewModel.toggleVoiceCommand() }
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
                                text = "Some critical permissions are missing. Alarms and timers may not work reliably.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Notifications permission
            item {
                PermissionItem(
                    title = "Notifications",
                    subtitle = "Required for alarm and timer alerts",
                    isGranted = permissionStatus.notificationsEnabled,
                    onClick = { permissionsHelper.openNotificationSettings() }
                )
            }

            // Exact alarms permission
            item {
                PermissionItem(
                    title = "Schedule Exact Alarms",
                    subtitle = "Required for precise alarm timing",
                    isGranted = permissionStatus.canScheduleExactAlarms,
                    onClick = { permissionsHelper.openAlarmSettings() }
                )
            }

            // Full-screen intent permission
            item {
                PermissionItem(
                    title = "Full-Screen Notifications",
                    subtitle = "Show alarms when phone is locked",
                    isGranted = permissionStatus.canUseFullScreenIntent,
                    onClick = { permissionsHelper.openNotificationSettings() }
                )
            }

            // Battery optimization
            item {
                PermissionItem(
                    title = "Battery Optimization",
                    subtitle = "Disable for reliable background operation",
                    isGranted = permissionStatus.batteryOptimizationDisabled,
                    onClick = { permissionsHelper.openBatteryOptimizationSettings() }
                )
            }

            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Experimental View Settings
            if (state.settings.uiMode == UiMode.EXPERIMENTAL) {
                item {
                    SettingsSectionHeader(title = "Experimental View")
                }

                item {
                    SettingsItem(
                        title = "Recent alarms count",
                        subtitle = "${state.settings.maxRecentAlarms} alarms",
                        onClick = { /* TODO: Show number picker */ }
                    )
                }

                item {
                    SettingsItem(
                        title = "Recent timers count",
                        subtitle = "${state.settings.maxRecentTimers} timers",
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
                    subtitle = "1.0.0",
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
        leadingContent = {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (isGranted) "Granted" else "Not granted",
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        trailingContent = {
            if (!isGranted) {
                OutlinedButton(onClick = onClick) {
                    Text("Open Settings")
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 12.dp),
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
                                    UiMode.CLASSIC -> "Traditional layout with tabs"
                                    UiMode.EXPERIMENTAL -> "All features on one screen with voice button"
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
