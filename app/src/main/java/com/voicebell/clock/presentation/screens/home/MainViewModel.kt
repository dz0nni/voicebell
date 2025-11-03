package com.voicebell.clock.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.domain.usecase.alarm.GetAlarmsUseCase
import com.voicebell.clock.domain.usecase.settings.GetSettingsUseCase
import com.voicebell.clock.domain.usecase.settings.UpdateUiModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateUiModeUseCase: UpdateUiModeUseCase,
    private val getAlarmsUseCase: GetAlarmsUseCase
    // TODO: Add GetTimersUseCase when implemented
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

        // TODO: Load recent timers when TimerUseCase is implemented
        // getTimersUseCase()
        //     .onEach { timers ->
        //         val maxRecent = _state.value.settings.maxRecentTimers
        //         val recentTimers = timers.take(maxRecent)
        //         _state.update { it.copy(recentTimers = recentTimers) }
        //     }
        //     .launchIn(viewModelScope)
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
}
