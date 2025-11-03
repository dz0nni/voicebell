package com.voicebell.clock.presentation.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.Stopwatch
import com.voicebell.clock.domain.usecase.stopwatch.AddLapUseCase
import com.voicebell.clock.domain.usecase.stopwatch.GetStopwatchUseCase
import com.voicebell.clock.domain.usecase.stopwatch.PauseStopwatchUseCase
import com.voicebell.clock.domain.usecase.stopwatch.ResetStopwatchUseCase
import com.voicebell.clock.domain.usecase.stopwatch.StartStopwatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Stopwatch screen following MVI pattern.
 */
@HiltViewModel
class StopwatchViewModel @Inject constructor(
    private val getStopwatchUseCase: GetStopwatchUseCase,
    private val startStopwatchUseCase: StartStopwatchUseCase,
    private val pauseStopwatchUseCase: PauseStopwatchUseCase,
    private val resetStopwatchUseCase: ResetStopwatchUseCase,
    private val addLapUseCase: AddLapUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StopwatchState())
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    private val _effect = Channel<StopwatchEffect>()
    val effect = _effect.receiveAsFlow()

    private var updateJob: Job? = null

    init {
        observeStopwatch()
    }

    /**
     * Observes stopwatch state from repository.
     */
    private fun observeStopwatch() {
        viewModelScope.launch {
            getStopwatchUseCase.invoke().collect { stopwatch ->
                if (stopwatch == null) {
                    _state.update {
                        it.copy(
                            currentElapsedMillis = 0L,
                            laps = emptyList(),
                            isRunning = false,
                            isPaused = false,
                            isLoading = false
                        )
                    }
                    stopRealTimeUpdates()
                } else {
                    _state.update {
                        it.copy(
                            currentElapsedMillis = stopwatch.getCurrentElapsedMillis(),
                            laps = stopwatch.laps,
                            isRunning = stopwatch.isRunning,
                            isPaused = stopwatch.isPaused,
                            isLoading = false
                        )
                    }

                    // Start real-time updates if running
                    if (stopwatch.isRunning && !stopwatch.isPaused) {
                        startRealTimeUpdates()
                    } else {
                        stopRealTimeUpdates()
                    }
                }
            }
        }
    }

    /**
     * Starts a coroutine that updates elapsed time every 10ms for smooth UI.
     */
    private fun startRealTimeUpdates() {
        if (updateJob?.isActive == true) return

        updateJob = viewModelScope.launch {
            while (true) {
                delay(10L) // Update every 10ms for smooth UI

                // Recalculate current elapsed time
                viewModelScope.launch {
                    val currentState = getStopwatchUseCase.getOnce()
                    if (currentState != null && currentState.isRunning && !currentState.isPaused) {
                        _state.update {
                            it.copy(currentElapsedMillis = currentState.getCurrentElapsedMillis())
                        }
                    }
                }
            }
        }
    }

    /**
     * Stops the real-time update coroutine.
     */
    private fun stopRealTimeUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    /**
     * Handles events from the UI.
     */
    fun onEvent(event: StopwatchEvent) {
        when (event) {
            is StopwatchEvent.Start -> startStopwatch()
            is StopwatchEvent.Pause -> pauseStopwatch()
            is StopwatchEvent.Reset -> resetStopwatch()
            is StopwatchEvent.AddLap -> addLap()
        }
    }

    /**
     * Starts or resumes the stopwatch.
     */
    private fun startStopwatch() {
        viewModelScope.launch {
            startStopwatchUseCase.invoke()
                .onSuccess {
                    // Success - state will update via Flow
                }
                .onFailure { error ->
                    _effect.send(
                        StopwatchEffect.ShowError(
                            error.message ?: "Failed to start stopwatch"
                        )
                    )
                }
        }
    }

    /**
     * Pauses the stopwatch.
     */
    private fun pauseStopwatch() {
        viewModelScope.launch {
            pauseStopwatchUseCase.invoke()
                .onSuccess {
                    // Success - state will update via Flow
                }
                .onFailure { error ->
                    _effect.send(
                        StopwatchEffect.ShowError(
                            error.message ?: "Failed to pause stopwatch"
                        )
                    )
                }
        }
    }

    /**
     * Resets the stopwatch to initial state.
     */
    private fun resetStopwatch() {
        viewModelScope.launch {
            resetStopwatchUseCase.invoke()
                .onSuccess {
                    // Success - state will update via Flow
                }
                .onFailure { error ->
                    _effect.send(
                        StopwatchEffect.ShowError(
                            error.message ?: "Failed to reset stopwatch"
                        )
                    )
                }
        }
    }

    /**
     * Adds a lap to the stopwatch.
     */
    private fun addLap() {
        viewModelScope.launch {
            addLapUseCase.invoke()
                .onSuccess {
                    // Success - state will update via Flow
                }
                .onFailure { error ->
                    _effect.send(
                        StopwatchEffect.ShowError(
                            error.message ?: "Failed to add lap"
                        )
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRealTimeUpdates()
    }
}

/**
 * UI state for Stopwatch screen.
 */
data class StopwatchState(
    val currentElapsedMillis: Long = 0L,
    val laps: List<Stopwatch.Lap> = emptyList(),
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * Events from UI to ViewModel.
 */
sealed class StopwatchEvent {
    object Start : StopwatchEvent()
    object Pause : StopwatchEvent()
    object Reset : StopwatchEvent()
    object AddLap : StopwatchEvent()
}

/**
 * One-time effects from ViewModel to UI.
 */
sealed class StopwatchEffect {
    data class ShowError(val message: String) : StopwatchEffect()
}
