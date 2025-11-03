package com.voicebell.clock.presentation.screens.alarm

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.usecase.alarm.DeleteAlarmUseCase
import com.voicebell.clock.domain.usecase.alarm.GetAlarmsUseCase
import com.voicebell.clock.domain.usecase.alarm.ToggleAlarmUseCase
import com.voicebell.clock.util.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Alarm list screen.
 * Implements MVI pattern with unidirectional data flow.
 */
@HiltViewModel
class AlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    companion object {
        private const val TAG = "AlarmViewModel"
    }

    // State
    private val _state = MutableStateFlow(AlarmState())
    val state: StateFlow<AlarmState> = _state.asStateFlow()

    // Effects (one-time events)
    private val _effects = Channel<AlarmEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadAlarms()
        checkExactAlarmPermission()
    }

    /**
     * Process user events
     */
    fun onEvent(event: AlarmEvent) {
        when (event) {
            is AlarmEvent.ToggleAlarm -> toggleAlarm(event.alarmId, event.isEnabled)
            is AlarmEvent.EditAlarm -> navigateToEditAlarm(event.alarmId)
            is AlarmEvent.DeleteAlarm -> showDeleteDialog(event.alarmId)
            is AlarmEvent.ConfirmDelete -> confirmDeleteAlarm(event.alarmId)
            is AlarmEvent.CancelDelete -> cancelDeleteAlarm()
            is AlarmEvent.CreateNewAlarm -> navigateToCreateAlarm()
            is AlarmEvent.RefreshAlarms -> loadAlarms()
            is AlarmEvent.DismissError -> dismissError()
            is AlarmEvent.OpenSettings -> navigateToSettings()
        }
    }

    /**
     * Load all alarms from repository
     */
    private fun loadAlarms() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        getAlarmsUseCase()
            .onEach { alarms ->
                _state.update {
                    it.copy(
                        alarms = alarms,
                        isLoading = false,
                        showEmptyState = alarms.isEmpty(),
                        nextAlarm = getNextAlarm(alarms)
                    )
                }
            }
            .catch { error ->
                Log.e(TAG, "Error loading alarms", error)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load alarms: ${error.message}"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Toggle alarm enabled/disabled
     */
    private fun toggleAlarm(alarmId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                toggleAlarmUseCase(alarmId, isEnabled)
                    .onSuccess {
                        // Success - state will be updated by flow
                        val message = if (isEnabled) "Alarm enabled" else "Alarm disabled"
                        _effects.send(AlarmEffect.ShowSuccess(message))
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error toggling alarm", error)
                        _effects.send(
                            AlarmEffect.ShowError("Failed to update alarm: ${error.message}")
                        )
                        // Revert toggle in UI by reloading
                        loadAlarms()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling alarm", e)
                _effects.send(AlarmEffect.ShowError("Failed to update alarm"))
            }
        }
    }

    /**
     * Show delete confirmation dialog
     */
    private fun showDeleteDialog(alarmId: Long) {
        _state.update {
            it.copy(
                alarmPendingDelete = alarmId,
                showDeleteDialog = true
            )
        }
    }

    /**
     * Cancel delete operation
     */
    private fun cancelDeleteAlarm() {
        _state.update {
            it.copy(
                alarmPendingDelete = null,
                showDeleteDialog = false
            )
        }
    }

    /**
     * Confirm and execute alarm deletion
     */
    private fun confirmDeleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                // Find alarm before deleting for undo functionality
                val alarm = _state.value.alarms.find { it.id == alarmId }

                deleteAlarmUseCase(alarmId)
                    .onSuccess {
                        // Hide dialog
                        _state.update {
                            it.copy(
                                alarmPendingDelete = null,
                                showDeleteDialog = false
                            )
                        }

                        // Show snackbar with undo option
                        if (alarm != null) {
                            _effects.send(AlarmEffect.ShowAlarmDeleted(alarmId, alarm))
                        } else {
                            _effects.send(AlarmEffect.ShowSuccess("Alarm deleted"))
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error deleting alarm", error)
                        _effects.send(
                            AlarmEffect.ShowError("Failed to delete alarm: ${error.message}")
                        )
                        // Hide dialog
                        _state.update {
                            it.copy(
                                alarmPendingDelete = null,
                                showDeleteDialog = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting alarm", e)
                _effects.send(AlarmEffect.ShowError("Failed to delete alarm"))
            }
        }
    }

    /**
     * Navigate to create alarm screen
     */
    private fun navigateToCreateAlarm() {
        viewModelScope.launch {
            _effects.send(AlarmEffect.NavigateToCreateAlarm)
        }
    }

    /**
     * Navigate to edit alarm screen
     */
    private fun navigateToEditAlarm(alarmId: Long) {
        viewModelScope.launch {
            _effects.send(AlarmEffect.NavigateToEditAlarm(alarmId))
        }
    }

    /**
     * Navigate to settings screen
     */
    private fun navigateToSettings() {
        viewModelScope.launch {
            _effects.send(AlarmEffect.NavigateToSettings)
        }
    }

    /**
     * Dismiss error message
     */
    private fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Check if app has permission to schedule exact alarms
     */
    private fun checkExactAlarmPermission() {
        val hasPermission = alarmScheduler.canScheduleExactAlarms()
        _state.update { it.copy(hasExactAlarmPermission = hasPermission) }

        if (!hasPermission) {
            viewModelScope.launch {
                _effects.send(
                    AlarmEffect.ShowError(
                        "Permission required: Enable 'Alarms & reminders' in app settings"
                    )
                )
            }
        }
    }

    /**
     * Get next scheduled alarm from list
     */
    private fun getNextAlarm(alarms: List<Alarm>): Alarm? {
        return alarms
            .filter { it.isEnabled }
            .minByOrNull { it.getNextTriggerTime() }
    }
}
