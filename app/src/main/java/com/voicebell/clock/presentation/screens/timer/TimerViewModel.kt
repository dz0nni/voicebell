package com.voicebell.clock.presentation.screens.timer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.usecase.timer.*
import com.voicebell.clock.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Timer screen
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getTimersUseCase: GetTimersUseCase,
    private val getActiveTimerUseCase: GetActiveTimerUseCase,
    private val startTimerUseCase: StartTimerUseCase,
    private val pauseTimerUseCase: PauseTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase,
    private val deleteTimerUseCase: DeleteTimerUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "TimerViewModel"
    }

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    init {
        loadTimers()
    }

    /**
     * Load timers and active timer
     */
    private fun loadTimers() {
        // Load all timers
        getTimersUseCase()
            .onEach { timers ->
                _state.update { it.copy(timers = timers) }
            }
            .launchIn(viewModelScope)

        // Load active timer
        getActiveTimerUseCase()
            .onEach { activeTimers ->
                val activeTimer = activeTimers.firstOrNull()
                _state.update { it.copy(activeTimer = activeTimer) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Handle user events
     */
    fun onEvent(event: TimerEvent) {
        when (event) {
            is TimerEvent.SetHours -> {
                _state.update { it.copy(inputHours = event.hours.coerceIn(0, 99)) }
            }
            is TimerEvent.SetMinutes -> {
                _state.update { it.copy(inputMinutes = event.minutes.coerceIn(0, 59)) }
            }
            is TimerEvent.SetSeconds -> {
                _state.update { it.copy(inputSeconds = event.seconds.coerceIn(0, 59)) }
            }
            is TimerEvent.SetLabel -> {
                _state.update { it.copy(inputLabel = event.label) }
            }
            is TimerEvent.ToggleVibrate -> {
                _state.update { it.copy(vibrateEnabled = event.enabled) }
            }
            is TimerEvent.StartTimer -> {
                startNewTimer()
            }
            is TimerEvent.PauseTimer -> {
                pauseActiveTimer()
            }
            is TimerEvent.ResumeTimer -> {
                resumeActiveTimer()
            }
            is TimerEvent.StopTimer -> {
                stopActiveTimer()
            }
            is TimerEvent.RestartTimer -> {
                restartTimer(event.timer)
            }
            is TimerEvent.DeleteTimer -> {
                deleteTimer(event.timerId)
            }
            is TimerEvent.DismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    /**
     * Start a new timer
     */
    private fun startNewTimer() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.inputDurationMillis <= 0) {
                _state.update { it.copy(errorMessage = "Please enter a duration") }
                return@launch
            }

            if (currentState.activeTimer != null) {
                _state.update { it.copy(errorMessage = "A timer is already running") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                startTimerUseCase(
                    durationMillis = currentState.inputDurationMillis,
                    label = currentState.inputLabel,
                    vibrate = currentState.vibrateEnabled
                ).onSuccess { timerId ->
                    Log.d(TAG, "Timer started: $timerId")

                    // Start TimerService
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_START
                        putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                    }
                    context.startForegroundService(intent)

                    // Reset input
                    _state.update {
                        it.copy(
                            isLoading = false,
                            inputHours = 0,
                            inputMinutes = 0,
                            inputSeconds = 0,
                            inputLabel = ""
                        )
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Error starting timer", error)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to start timer: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting timer", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to start timer: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Pause active timer
     */
    private fun pauseActiveTimer() {
        viewModelScope.launch {
            val activeTimer = _state.value.activeTimer
            if (activeTimer == null) {
                _state.update { it.copy(errorMessage = "No timer is running") }
                return@launch
            }

            try {
                pauseTimerUseCase(activeTimer.id)
                    .onSuccess {
                        Log.d(TAG, "Timer paused: ${activeTimer.id}")

                        // Notify service
                        val intent = Intent(context, TimerService::class.java).apply {
                            action = TimerService.ACTION_PAUSE
                        }
                        context.startService(intent)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error pausing timer", error)
                        _state.update {
                            it.copy(errorMessage = "Failed to pause timer: ${error.message}")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing timer", e)
                _state.update {
                    it.copy(errorMessage = "Failed to pause timer: ${e.message}")
                }
            }
        }
    }

    /**
     * Resume paused timer
     */
    private fun resumeActiveTimer() {
        viewModelScope.launch {
            val activeTimer = _state.value.activeTimer
            if (activeTimer == null) {
                _state.update { it.copy(errorMessage = "No timer to resume") }
                return@launch
            }

            try {
                startTimerUseCase.resume(activeTimer.id)
                    .onSuccess {
                        Log.d(TAG, "Timer resumed: ${activeTimer.id}")

                        // Notify service
                        val intent = Intent(context, TimerService::class.java).apply {
                            action = TimerService.ACTION_RESUME
                        }
                        context.startService(intent)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error resuming timer", error)
                        _state.update {
                            it.copy(errorMessage = "Failed to resume timer: ${error.message}")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming timer", e)
                _state.update {
                    it.copy(errorMessage = "Failed to resume timer: ${e.message}")
                }
            }
        }
    }

    /**
     * Stop active timer
     */
    private fun stopActiveTimer() {
        viewModelScope.launch {
            val activeTimer = _state.value.activeTimer
            if (activeTimer == null) {
                _state.update { it.copy(errorMessage = "No timer is running") }
                return@launch
            }

            try {
                stopTimerUseCase(activeTimer.id)
                    .onSuccess {
                        Log.d(TAG, "Timer stopped: ${activeTimer.id}")

                        // Stop service
                        val intent = Intent(context, TimerService::class.java).apply {
                            action = TimerService.ACTION_STOP
                        }
                        context.startService(intent)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error stopping timer", error)
                        _state.update {
                            it.copy(errorMessage = "Failed to stop timer: ${error.message}")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping timer", e)
                _state.update {
                    it.copy(errorMessage = "Failed to stop timer: ${e.message}")
                }
            }
        }
    }

    /**
     * Restart a previous timer with same duration
     */
    private fun restartTimer(timer: Timer) {
        viewModelScope.launch {
            if (_state.value.activeTimer != null) {
                _state.update { it.copy(errorMessage = "A timer is already running") }
                return@launch
            }

            try {
                startTimerUseCase(
                    durationMillis = timer.durationMillis,
                    label = timer.label,
                    vibrate = timer.vibrate
                ).onSuccess { timerId ->
                    Log.d(TAG, "Timer restarted: $timerId")

                    // Start TimerService
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_START
                        putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                    }
                    context.startForegroundService(intent)
                }.onFailure { error ->
                    Log.e(TAG, "Error restarting timer", error)
                    _state.update {
                        it.copy(errorMessage = "Failed to restart timer: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restarting timer", e)
                _state.update {
                    it.copy(errorMessage = "Failed to restart timer: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete a timer
     */
    private fun deleteTimer(timerId: Long) {
        viewModelScope.launch {
            try {
                deleteTimerUseCase(timerId)
                    .onSuccess {
                        Log.d(TAG, "Timer deleted: $timerId")
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error deleting timer", error)
                        _state.update {
                            it.copy(errorMessage = "Failed to delete timer: ${error.message}")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting timer", e)
                _state.update {
                    it.copy(errorMessage = "Failed to delete timer: ${e.message}")
                }
            }
        }
    }
}
