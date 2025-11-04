package com.voicebell.clock.presentation.screens.alarm.edit

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.usecase.alarm.CreateAlarmUseCase
import com.voicebell.clock.domain.usecase.alarm.DeleteAlarmUseCase
import com.voicebell.clock.domain.usecase.alarm.GetAlarmByIdUseCase
import com.voicebell.clock.domain.usecase.alarm.ScheduleAlarmUseCase
import com.voicebell.clock.domain.usecase.alarm.UpdateAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel for alarm edit/create screen
 */
@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlarmByIdUseCase: GetAlarmByIdUseCase,
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val updateAlarmUseCase: UpdateAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val scheduleAlarmUseCase: ScheduleAlarmUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "AlarmEditViewModel"
        const val ARG_ALARM_ID = "alarmId"
    }

    private val _state = MutableStateFlow(AlarmEditState())
    val state: StateFlow<AlarmEditState> = _state.asStateFlow()

    private val _effects = Channel<AlarmEditEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        // Check if we're editing an existing alarm
        val alarmId = savedStateHandle.get<Long>(ARG_ALARM_ID)
        if (alarmId != null && alarmId > 0L) {
            loadAlarm(alarmId)
        }
    }

    /**
     * Load existing alarm for editing
     */
    private fun loadAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                val alarm = getAlarmByIdUseCase.getOnce(alarmId)
                if (alarm != null) {
                    _state.update {
                        it.copy(
                            alarmId = alarm.id,
                            isEditMode = true,
                            hour = alarm.time.hour,
                            minute = alarm.time.minute,
                            label = alarm.label,
                            repeatDays = alarm.repeatDays,
                            alarmTone = alarm.alarmTone,
                            vibrate = alarm.vibrate,
                            flash = alarm.flash,
                            gradualVolumeIncrease = alarm.gradualVolumeIncrease,
                            volumeLevel = alarm.volumeLevel,
                            snoozeEnabled = alarm.snoozeEnabled,
                            snoozeDuration = alarm.snoozeDuration,
                            maxSnoozeCount = alarm.maxSnoozeCount,
                            preAlarmCount = alarm.preAlarmCount,
                            preAlarmInterval = alarm.preAlarmInterval
                        )
                    }
                } else {
                    Log.e(TAG, "Alarm not found: $alarmId")
                    _effects.send(AlarmEditEffect.NavigateBack)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading alarm", e)
                _state.update { it.copy(errorMessage = "Failed to load alarm") }
            }
        }
    }

    /**
     * Handle user events
     */
    fun onEvent(event: AlarmEditEvent) {
        when (event) {
            is AlarmEditEvent.SetHour -> {
                _state.update { it.copy(hour = event.hour) }
            }
            is AlarmEditEvent.SetMinute -> {
                _state.update { it.copy(minute = event.minute) }
            }
            is AlarmEditEvent.SetLabel -> {
                _state.update { it.copy(label = event.label) }
            }
            is AlarmEditEvent.ToggleRepeatDay -> {
                val currentDays = _state.value.repeatDays
                val newDays = if (currentDays.contains(event.day)) {
                    currentDays - event.day
                } else {
                    currentDays + event.day
                }
                _state.update { it.copy(repeatDays = newDays) }
            }
            is AlarmEditEvent.SetAlarmTone -> {
                // TODO: Parse AlarmTone from string when AlarmTone picker is implemented
                _state.update { it.copy(alarmTone = it.alarmTone) }
            }
            is AlarmEditEvent.ToggleVibrate -> {
                _state.update { it.copy(vibrate = event.enabled) }
            }
            is AlarmEditEvent.ToggleFlash -> {
                _state.update { it.copy(flash = event.enabled) }
            }
            is AlarmEditEvent.ToggleGradualVolume -> {
                _state.update { it.copy(gradualVolumeIncrease = event.enabled) }
            }
            is AlarmEditEvent.SetVolumeLevel -> {
                _state.update { it.copy(volumeLevel = event.level.coerceIn(0, 100)) }
            }
            is AlarmEditEvent.ToggleSnooze -> {
                _state.update { it.copy(snoozeEnabled = event.enabled) }
            }
            is AlarmEditEvent.SetSnoozeDuration -> {
                _state.update { it.copy(snoozeDuration = event.minutes.coerceIn(1, 60)) }
            }
            is AlarmEditEvent.SetMaxSnoozeCount -> {
                _state.update { it.copy(maxSnoozeCount = event.count.coerceIn(1, 10)) }
            }
            is AlarmEditEvent.SetPreAlarmCount -> {
                _state.update { it.copy(preAlarmCount = event.count.coerceIn(0, 10)) }
            }
            is AlarmEditEvent.SetPreAlarmInterval -> {
                _state.update { it.copy(preAlarmInterval = event.minutes.coerceIn(1, 30)) }
            }
            is AlarmEditEvent.SaveAlarm -> {
                saveAlarm()
            }
            is AlarmEditEvent.Cancel -> {
                navigateBack()
            }
            is AlarmEditEvent.DeleteAlarm -> {
                _state.update { it.copy(showDeleteDialog = true) }
            }
        }
    }

    /**
     * Save alarm (create or update)
     */
    private fun saveAlarm() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                val currentState = _state.value
                val alarm = Alarm(
                    id = currentState.alarmId ?: 0,
                    time = LocalTime.of(currentState.hour, currentState.minute),
                    isEnabled = true, // New/edited alarms are enabled by default
                    label = currentState.label,
                    alarmTone = currentState.alarmTone,
                    repeatDays = currentState.repeatDays,
                    vibrate = currentState.vibrate,
                    flash = currentState.flash,
                    gradualVolumeIncrease = currentState.gradualVolumeIncrease,
                    volumeLevel = currentState.volumeLevel,
                    snoozeEnabled = currentState.snoozeEnabled,
                    snoozeDuration = currentState.snoozeDuration,
                    snoozeCount = 0, // Reset snooze count
                    maxSnoozeCount = currentState.maxSnoozeCount,
                    preAlarmCount = currentState.preAlarmCount,
                    preAlarmInterval = currentState.preAlarmInterval
                )

                if (currentState.isEditMode) {
                    // Update existing alarm
                    updateAlarmUseCase(alarm)
                        .onSuccess {
                            scheduleAlarmUseCase(alarm)
                            _effects.send(AlarmEditEffect.ShowSuccess("Alarm saved"))
                            _effects.send(AlarmEditEffect.NavigateBack)
                        }
                        .onFailure { error ->
                            Log.e(TAG, "Error updating alarm", error)
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = "Failed to save alarm: ${error.message}"
                                )
                            }
                        }
                } else {
                    // Create new alarm
                    createAlarmUseCase(alarm)
                        .onSuccess { alarmId ->
                            val savedAlarm = alarm.copy(id = alarmId)
                            scheduleAlarmUseCase(savedAlarm)
                            _effects.send(AlarmEditEffect.ShowSuccess("Alarm saved"))
                            _effects.send(AlarmEditEffect.NavigateBack)
                        }
                        .onFailure { error ->
                            Log.e(TAG, "Error creating alarm", error)
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = "Failed to save alarm: ${error.message}"
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving alarm", e)
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save alarm: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Delete alarm
     */
    fun confirmDelete() {
        viewModelScope.launch {
            val alarmId = _state.value.alarmId
            if (alarmId != null) {
                try {
                    deleteAlarmUseCase(alarmId)
                        .onSuccess {
                            _effects.send(AlarmEditEffect.ShowSuccess("Alarm deleted"))
                            _effects.send(AlarmEditEffect.NavigateBack)
                        }
                        .onFailure { error ->
                            Log.e(TAG, "Error deleting alarm", error)
                            _state.update {
                                it.copy(errorMessage = "Failed to delete alarm: ${error.message}")
                            }
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting alarm", e)
                    _state.update {
                        it.copy(errorMessage = "Failed to delete alarm: ${e.message}")
                    }
                }
            }
            _state.update { it.copy(showDeleteDialog = false) }
        }
    }

    /**
     * Cancel delete
     */
    fun cancelDelete() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    /**
     * Navigate back
     */
    private fun navigateBack() {
        viewModelScope.launch {
            _effects.send(AlarmEditEffect.NavigateBack)
        }
    }
}

/**
 * One-time effects for navigation and feedback
 */
sealed class AlarmEditEffect {
    data object NavigateBack : AlarmEditEffect()
    data class ShowSuccess(val message: String) : AlarmEditEffect()
    data class ShowError(val message: String) : AlarmEditEffect()
}
