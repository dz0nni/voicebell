package com.voicebell.clock.presentation.screens.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.DayOfWeek

/**
 * Material Design 3 card component for displaying an alarm.
 * Shows time, label, repeat days, and controls for enabling/editing/deleting.
 */
@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Time and toggle switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time display (clickable to edit)
                Text(
                    text = alarm.getFormattedTime(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.clickable { onEdit() }
                )

                // Enable/disable switch
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Label
            if (alarm.label.isNotBlank()) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Repeat days
            val repeatDaysText = getRepeatDaysText(alarm.repeatDays)
            Text(
                text = repeatDaysText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (alarm.isEnabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )

            // Alarm features (visible when expanded)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature chips
                    AlarmFeatureChips(alarm)
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expand/Collapse button
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (expanded) "Show less" else "Show more"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Edit button
                FilledTonalButton(
                    onClick = onEdit,
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                // Delete button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Display alarm feature indicators as chips
 */
@Composable
private fun AlarmFeatureChips(alarm: Alarm) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Pre-alarms
        if (alarm.preAlarmCount > 0) {
            FeatureChip(
                icon = Icons.Default.Notifications,
                text = "${alarm.preAlarmCount} pre-alarm(s), ${alarm.preAlarmInterval} min apart"
            )
        }

        // Vibration
        if (alarm.vibrate) {
            FeatureChip(
                icon = Icons.Default.Vibration,
                text = "Vibration enabled"
            )
        }

        // Flash
        if (alarm.flash) {
            FeatureChip(
                icon = Icons.Default.FlashOn,
                text = "Flash enabled"
            )
        }

        // Gradual volume
        if (alarm.gradualVolumeIncrease) {
            FeatureChip(
                icon = Icons.Default.VolumeUp,
                text = "Gradual volume increase"
            )
        }

        // Snooze
        if (alarm.snoozeEnabled) {
            FeatureChip(
                icon = Icons.Default.Snooze,
                text = "Snooze: ${alarm.snoozeDuration} min (max ${alarm.maxSnoozeCount}x)"
            )
        }

        // Alarm tone
        FeatureChip(
            icon = Icons.Default.MusicNote,
            text = "Tone: ${alarm.alarmTone.displayName}"
        )
    }
}

/**
 * Small chip to display a single alarm feature
 */
@Composable
private fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Convert repeat days to readable text
 */
private fun getRepeatDaysText(repeatDays: Set<DayOfWeek>): String {
    if (repeatDays.isEmpty()) {
        return "One time"
    }

    // Check for every day
    if (repeatDays == DayOfWeek.ALL_DAYS) {
        return "Every day"
    }

    // Check for weekdays
    if (repeatDays == DayOfWeek.WEEKDAYS) {
        return "Weekdays"
    }

    // Check for weekends
    if (repeatDays == DayOfWeek.WEEKENDS) {
        return "Weekends"
    }

    // Display abbreviated day names
    return repeatDays.sortedBy { it.value }
        .joinToString(", ") { it.shortName }
}
