package com.voicebell.clock.presentation.screens.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.repository.AlarmRepository
import com.voicebell.clock.service.AlarmService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AlarmRingingActivity.
 * Loads alarm details to display on the ringing screen.
 */
@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _alarmLabel = MutableStateFlow("")
    val alarmLabel: StateFlow<String> = _alarmLabel.asStateFlow()

    init {
        // Get alarm ID from saved state
        val alarmId = savedStateHandle.get<Long>(AlarmService.EXTRA_ALARM_ID) ?: -1L

        if (alarmId != -1L) {
            loadAlarm(alarmId)
        }
    }

    private fun loadAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm != null && alarm.label.isNotBlank()) {
                    _alarmLabel.value = alarm.label
                }
            } catch (e: Exception) {
                // Ignore errors, just don't show a label
            }
        }
    }
}
