package com.voicebell.clock.presentation.screens.alarm.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicebell.clock.presentation.components.DayOfWeekSelector
import com.voicebell.clock.presentation.components.SelectedDaysText
import com.voicebell.clock.presentation.components.TimePicker

/**
 * Screen for creating or editing an alarm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlarmEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AlarmEditEffect.NavigateBack -> onNavigateBack()
                is AlarmEditEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is AlarmEditEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.screenTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(AlarmEditEvent.Cancel) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    if (state.isEditMode) {
                        IconButton(onClick = { viewModel.onEvent(AlarmEditEvent.DeleteAlarm) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Save button at bottom
            Surface(
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(AlarmEditEvent.Cancel) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { viewModel.onEvent(AlarmEditEvent.SaveAlarm) },
                        enabled = state.canSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Time Picker
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        TimePicker(
                            hour = state.hour,
                            minute = state.minute,
                            onHourChange = { viewModel.onEvent(AlarmEditEvent.SetHour(it)) },
                            onMinuteChange = { viewModel.onEvent(AlarmEditEvent.SetMinute(it)) }
                        )
                    }
                }
            }

            // Label
            item {
                OutlinedTextField(
                    value = state.label,
                    onValueChange = { viewModel.onEvent(AlarmEditEvent.SetLabel(it)) },
                    label = { Text("Label") },
                    placeholder = { Text("Morning alarm") },
                    leadingIcon = {
                        Icon(Icons.Default.Label, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Repeat Days
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Repeat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                SelectedDaysText(selectedDays = state.repeatDays)
                            }
                        }

                        DayOfWeekSelector(
                            selectedDays = state.repeatDays,
                            onDayToggle = { viewModel.onEvent(AlarmEditEvent.ToggleRepeatDay(it)) }
                        )
                    }
                }
            }

            // Sound & Vibration
            item {
                SettingsSection(title = "Sound & Vibration") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Vibration,
                        title = "Vibrate",
                        checked = state.vibrate,
                        onCheckedChange = { viewModel.onEvent(AlarmEditEvent.ToggleVibrate(it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsSwitchItem(
                        icon = Icons.Default.FlashOn,
                        title = "Flash",
                        checked = state.flash,
                        onCheckedChange = { viewModel.onEvent(AlarmEditEvent.ToggleFlash(it)) }
                    )
                }
            }

            // Volume Settings
            item {
                SettingsSection(title = "Volume") {
                    SettingsSwitchItem(
                        icon = Icons.Default.TrendingUp,
                        title = "Gradual volume increase",
                        subtitle = "Volume gradually increases over 60 seconds",
                        checked = state.gradualVolumeIncrease,
                        onCheckedChange = { viewModel.onEvent(AlarmEditEvent.ToggleGradualVolume(it)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Volume: ${state.volumeLevel}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Slider(
                        value = state.volumeLevel.toFloat(),
                        onValueChange = { viewModel.onEvent(AlarmEditEvent.SetVolumeLevel(it.toInt())) },
                        valueRange = 0f..100f,
                        modifier = Modifier.padding(start = 36.dp)
                    )
                }
            }

            // Snooze Settings
            item {
                SettingsSection(title = "Snooze") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Snooze,
                        title = "Enable snooze",
                        checked = state.snoozeEnabled,
                        onCheckedChange = { viewModel.onEvent(AlarmEditEvent.ToggleSnooze(it)) }
                    )

                    if (state.snoozeEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SettingsItem(
                            icon = Icons.Default.Timer,
                            title = "Snooze duration",
                            value = "${state.snoozeDuration} minutes",
                            onClick = { /* TODO: Show duration picker */ }
                        )

                        SettingsItem(
                            icon = Icons.Default.Numbers,
                            title = "Maximum snoozes",
                            value = "${state.maxSnoozeCount} times",
                            onClick = { /* TODO: Show count picker */ }
                        )
                    }
                }
            }

            // Pre-Alarms
            item {
                SettingsSection(title = "Pre-Alarms") {
                    Text(
                        text = "Gentle notifications before main alarm for deep sleepers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Number of pre-alarms",
                        value = if (state.preAlarmCount == 0) "Off" else "${state.preAlarmCount}",
                        onClick = { /* TODO: Show count picker */ }
                    )

                    if (state.preAlarmCount > 0) {
                        SettingsItem(
                            icon = Icons.Default.Schedule,
                            title = "Interval",
                            value = "${state.preAlarmInterval} minutes apart",
                            onClick = { /* TODO: Show interval picker */ }
                        )
                    }
                }
            }

            // Error message
            if (state.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Delete alarm?") },
            text = { Text("This alarm will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Settings section card with title
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

/**
 * Settings item with icon, title, and value
 */
@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Settings item with switch
 */
@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
