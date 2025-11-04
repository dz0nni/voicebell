package com.voicebell.clock.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voicebell.clock.domain.model.DayOfWeek

/**
 * Component for selecting days of the week.
 * Displays 7 chips (Mon-Sun) that can be toggled on/off.
 */
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<DayOfWeek>,
    onDayToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Days in order: Monday to Sunday
        val days = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )

        days.forEach { day ->
            DayChip(
                day = day,
                isSelected = selectedDays.contains(day),
                onClick = { onDayToggle(day) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual day chip
 */
@Composable
private fun DayChip(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = day.shortName.first().toString()

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier.height(48.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Helper text showing selected days in readable format
 */
@Composable
fun SelectedDaysText(
    selectedDays: Set<DayOfWeek>,
    modifier: Modifier = Modifier
) {
    val text = when {
        selectedDays.isEmpty() -> "One time"
        selectedDays == DayOfWeek.ALL_DAYS -> "Every day"
        selectedDays == DayOfWeek.WEEKDAYS -> "Weekdays"
        selectedDays == DayOfWeek.WEEKENDS -> "Weekends"
        else -> {
            val sortedDays = selectedDays.sortedBy { it.value }
            sortedDays.joinToString(", ") { it.shortName }
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
