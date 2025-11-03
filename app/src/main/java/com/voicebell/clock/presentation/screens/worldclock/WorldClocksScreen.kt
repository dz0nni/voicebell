package com.voicebell.clock.presentation.screens.worldclock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voicebell.clock.domain.model.WorldClock
import kotlinx.coroutines.delay
import java.time.ZoneId

/**
 * World Clocks screen showing time in different timezones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClocksScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorldClocksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WorldClocksEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is WorldClocksEffect.ShowError -> {
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
                title = { Text("World Clocks") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(WorldClocksEvent.ShowAddDialog) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add world clock"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.worldClocks.isEmpty() -> {
                    EmptyState(
                        onAddClick = { viewModel.onEvent(WorldClocksEvent.ShowAddDialog) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    WorldClocksList(
                        worldClocks = state.worldClocks,
                        onDelete = { worldClock ->
                            viewModel.onEvent(WorldClocksEvent.ShowDeleteDialog(worldClock))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Add Dialog
    if (state.showAddDialog) {
        AddWorldClockDialog(
            onDismiss = { viewModel.onEvent(WorldClocksEvent.DismissAddDialog) },
            onConfirm = { worldClock ->
                viewModel.onEvent(WorldClocksEvent.AddWorldClock(worldClock))
            }
        )
    }

    // Delete Confirmation Dialog
    if (state.showDeleteDialog && state.worldClockToDelete != null) {
        DeleteConfirmationDialog(
            worldClock = state.worldClockToDelete!!,
            onDismiss = { viewModel.onEvent(WorldClocksEvent.DismissDeleteDialog) },
            onConfirm = {
                viewModel.onEvent(WorldClocksEvent.DeleteWorldClock(state.worldClockToDelete!!.id))
            }
        )
    }
}

/**
 * List of world clocks.
 */
@Composable
private fun WorldClocksList(
    worldClocks: List<WorldClock>,
    onDelete: (WorldClock) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = worldClocks,
            key = { it.id }
        ) { worldClock ->
            WorldClockCard(
                worldClock = worldClock,
                onDelete = { onDelete(worldClock) }
            )
        }
    }
}

/**
 * Individual world clock card with real-time updating.
 */
@Composable
private fun WorldClockCard(
    worldClock: WorldClock,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Real-time update every second
    var currentTime by remember { mutableStateOf(worldClock.getFormattedTime(use24Hour = true)) }

    LaunchedEffect(worldClock.timeZoneId) {
        while (true) {
            currentTime = worldClock.getFormattedTime(use24Hour = true)
            delay(1000L)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // City name
                Text(
                    text = worldClock.cityName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Country name
                Text(
                    text = worldClock.countryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timezone offset
                Text(
                    text = worldClock.getTimezoneOffset(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Current time
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no world clocks added.
 */
@Composable
private fun EmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "No World Clocks",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Add clocks to see time in different timezones",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add World Clock")
        }
    }
}

/**
 * Dialog for adding a new world clock.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorldClockDialog(
    onDismiss: () -> Unit,
    onConfirm: (WorldClock) -> Unit
) {
    var selectedCity by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedTimezone by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    // Common timezones with cities
    val commonTimezones = remember {
        listOf(
            Triple("New York", "United States", "America/New_York"),
            Triple("Los Angeles", "United States", "America/Los_Angeles"),
            Triple("London", "United Kingdom", "Europe/London"),
            Triple("Paris", "France", "Europe/Paris"),
            Triple("Berlin", "Germany", "Europe/Berlin"),
            Triple("Tokyo", "Japan", "Asia/Tokyo"),
            Triple("Dubai", "UAE", "Asia/Dubai"),
            Triple("Sydney", "Australia", "Australia/Sydney"),
            Triple("Singapore", "Singapore", "Asia/Singapore"),
            Triple("Hong Kong", "China", "Asia/Hong_Kong"),
            Triple("Mumbai", "India", "Asia/Kolkata"),
            Triple("Moscow", "Russia", "Europe/Moscow"),
            Triple("Istanbul", "Turkey", "Europe/Istanbul"),
            Triple("São Paulo", "Brazil", "America/Sao_Paulo"),
            Triple("Mexico City", "Mexico", "America/Mexico_City")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add World Clock") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = showDropdown,
                    onExpandedChange = { showDropdown = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedCity.isNotEmpty()) "$selectedCity, $selectedCountry" else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select City") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        commonTimezones.forEach { (city, country, timezone) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = city,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = country,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCity = city
                                    selectedCountry = country
                                    selectedTimezone = timezone
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedTimezone.isNotEmpty()) {
                        onConfirm(
                            WorldClock(
                                cityName = selectedCity,
                                countryName = selectedCountry,
                                timeZoneId = selectedTimezone
                            )
                        )
                    }
                },
                enabled = selectedTimezone.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Delete confirmation dialog.
 */
@Composable
private fun DeleteConfirmationDialog(
    worldClock: WorldClock,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete World Clock?") },
        text = {
            Text("Are you sure you want to delete ${worldClock.cityName}, ${worldClock.countryName}?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
