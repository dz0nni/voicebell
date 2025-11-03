package com.voicebell.clock.presentation.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Material Design 3 time picker component.
 * Displays hour and minute pickers side by side.
 */
@Composable
fun TimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour picker
        NumberPicker(
            value = hour,
            range = if (is24Hour) 0..23 else 1..12,
            onValueChange = onHourChange,
            modifier = Modifier.weight(1f)
        )

        // Separator
        Text(
            text = ":",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Minute picker
        NumberPicker(
            value = minute,
            range = 0..59,
            onValueChange = onMinuteChange,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Scrollable number picker for selecting a value from a range
 */
@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(0, value - range.first - 1)
    )
    val coroutineScope = rememberCoroutineScope()

    // Update selection when scrolling stops
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex + 1
            val newValue = range.first + centerIndex
            if (newValue in range && newValue != value) {
                onValueChange(newValue)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection indicator background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {}

        // Scrollable list of numbers
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 76.dp) // Center the selected item
        ) {
            items(range.count()) { index ->
                val itemValue = range.first + index
                val isSelected = itemValue == value

                Text(
                    text = String.format("%02d", itemValue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}
