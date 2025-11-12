package com.voicebell.clock.presentation.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.domain.usecase.alarm.GetAlarmsUseCase
import com.voicebell.clock.domain.usecase.settings.GetSettingsUseCase
import com.voicebell.clock.domain.usecase.settings.UpdateUiModeUseCase
import com.voicebell.clock.domain.repository.TimerRepository
import com.voicebell.clock.domain.usecase.timer.GetTimersUseCase
import com.voicebell.clock.domain.usecase.timer.StartTimerUseCase
import com.voicebell.clock.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for main screen.
 * Manages UI mode switching and provides data for both layouts.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateUiModeUseCase: UpdateUiModeUseCase,
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val getTimersUseCase: GetTimersUseCase,
    private val toggleAlarmUseCase: com.voicebell.clock.domain.usecase.alarm.ToggleAlarmUseCase,
    private val deleteAlarmUseCase: com.voicebell.clock.domain.usecase.alarm.DeleteAlarmUseCase,
    private val startTimerUseCase: StartTimerUseCase,
    private val timerRepository: TimerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        loadSettings()
        loadRecentData()
    }

    /**
     * Load settings including UI mode
     */
    private fun loadSettings() {
        getSettingsUseCase()
            .onEach { settings ->
                _state.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Load recent alarms and timers for experimental view
     */
    private fun loadRecentData() {
        getAlarmsUseCase()
            .onEach { alarms ->
                val maxRecent = _state.value.settings.maxRecentAlarms
                val recentAlarms = alarms.take(maxRecent)
                _state.update { it.copy(recentAlarms = recentAlarms) }
            }
            .launchIn(viewModelScope)

        // Load recent timers
        getTimersUseCase()
            .onEach { timers ->
                val maxRecent = _state.value.settings.maxRecentTimers
                val recentTimers = timers.take(maxRecent)
                _state.update { it.copy(recentTimers = recentTimers) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Toggle UI mode between Classic and Experimental
     */
    fun toggleUiMode() {
        viewModelScope.launch {
            val newMode = when (_state.value.settings.uiMode) {
                UiMode.CLASSIC -> UiMode.EXPERIMENTAL
                UiMode.EXPERIMENTAL -> UiMode.CLASSIC
            }
            updateUiModeUseCase(newMode)
        }
    }

    /**
     * Set specific UI mode
     */
    fun setUiMode(uiMode: UiMode) {
        viewModelScope.launch {
            updateUiModeUseCase(uiMode)
        }
    }

    /**
     * Toggle alarm enabled state
     */
    fun toggleAlarm(alarmId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(alarmId, isEnabled)
        }
    }

    /**
     * Restart timer with its original duration
     */
    fun restartTimer(timerId: Long) {
        viewModelScope.launch {
            // Find the timer in recent timers
            val timer = _state.value.recentTimers.find { it.id == timerId }
            timer?.let {
                // Reset the timer to its original state
                val currentTime = System.currentTimeMillis()
                val restartedTimer = it.copy(
                    remainingMillis = it.durationMillis,
                    isRunning = true,
                    isPaused = false,
                    isFinished = false,
                    startTime = currentTime,
                    pauseTime = 0,
                    endTime = currentTime + it.durationMillis
                )

                // Update the existing timer
                timerRepository.updateTimer(restartedTimer)

                // Start TimerService with the same timer ID
                val intent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_START
                    putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                }
                context.startForegroundService(intent)
            }
        }
    }

    /**
     * Stop active timer
     */
    fun stopTimer(timerId: Long) {
        viewModelScope.launch {
            // Find the timer in recent timers
            val timer = _state.value.recentTimers.find { it.id == timerId }
            timer?.let {
                // Stop the timer
                val stoppedTimer = it.copy(
                    isRunning = false,
                    isPaused = false,
                    isFinished = false,
                    remainingMillis = it.durationMillis
                )

                // Update the timer
                timerRepository.updateTimer(stoppedTimer)

                // Stop TimerService
                val intent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP
                    putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                }
                context.startService(intent)
            }
        }
    }

    /**
     * Delete timer
     */
    fun deleteTimer(timerId: Long) {
        viewModelScope.launch {
            // Find the timer
            val timer = _state.value.recentTimers.find { it.id == timerId }
            timer?.let {
                // If timer is running, stop the service first
                if (it.isRunning) {
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_STOP
                        putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                    }
                    context.startService(intent)
                }

                // Delete the timer from repository
                timerRepository.deleteTimer(timerId)
            }
        }
    }

    /**
     * Delete alarm
     */
    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarmId)
        }
    }
}
