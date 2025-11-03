package com.voicebell.clock.presentation.screens.voice

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.service.VoiceRecognitionService
import com.voicebell.clock.util.VoiceCommandParser
import com.voicebell.clock.util.VoiceCommandResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Voice Command screen following MVI pattern.
 */
@HiltViewModel
class VoiceCommandViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voiceCommandParser: VoiceCommandParser
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceCommandState())
    val state: StateFlow<VoiceCommandState> = _state.asStateFlow()

    private val _effect = Channel<VoiceCommandEffect>()
    val effect = _effect.receiveAsFlow()

    /**
     * Handles events from the UI.
     */
    fun onEvent(event: VoiceCommandEvent) {
        when (event) {
            is VoiceCommandEvent.StartListening -> startListening()
            is VoiceCommandEvent.StopListening -> stopListening()
            is VoiceCommandEvent.RecognitionResult -> handleRecognitionResult(event.text)
            is VoiceCommandEvent.RecognitionError -> handleRecognitionError(event.error)
            is VoiceCommandEvent.PermissionDenied -> handlePermissionDenied()
        }
    }

    /**
     * Starts voice recognition service.
     */
    private fun startListening() {
        _state.update {
            it.copy(
                isListening = true,
                isParsing = false,
                permissionDenied = false,
                recognizedText = ""
            )
        }

        // Start voice recognition service
        val intent = Intent(context, VoiceRecognitionService::class.java).apply {
            action = VoiceRecognitionService.ACTION_START_RECOGNITION
        }
        context.startService(intent)
    }

    /**
     * Stops voice recognition service.
     */
    private fun stopListening() {
        _state.update {
            it.copy(isListening = false)
        }

        // Stop voice recognition service
        val intent = Intent(context, VoiceRecognitionService::class.java).apply {
            action = VoiceRecognitionService.ACTION_STOP_RECOGNITION
        }
        context.startService(intent)
    }

    /**
     * Handles recognition result from service.
     */
    private fun handleRecognitionResult(text: String) {
        _state.update {
            it.copy(
                recognizedText = text,
                isParsing = true,
                isListening = false
            )
        }

        // Parse the command
        viewModelScope.launch {
            val result = voiceCommandParser.parseCommand(text)

            when (result) {
                is VoiceCommandResult.AlarmCommand -> {
                    _effect.send(VoiceCommandEffect.ShowMessage("Creating alarm for ${result.time}"))
                    // TODO: Pass alarm data to alarm edit screen
                    _effect.send(VoiceCommandEffect.NavigateToAlarmEdit)
                }
                is VoiceCommandResult.TimerCommand -> {
                    val minutes = result.durationMillis / 60000
                    val seconds = (result.durationMillis % 60000) / 1000
                    _effect.send(VoiceCommandEffect.ShowMessage("Creating timer for ${minutes}m ${seconds}s"))
                    // TODO: Pass timer data to timer screen
                    _effect.send(VoiceCommandEffect.NavigateToTimer)
                }
                is VoiceCommandResult.Unknown -> {
                    _effect.send(VoiceCommandEffect.ShowError("Could not understand command: $text"))
                    _state.update { it.copy(isParsing = false) }
                }
                is VoiceCommandResult.Error -> {
                    _effect.send(VoiceCommandEffect.ShowError(result.message))
                    _state.update { it.copy(isParsing = false) }
                }
            }
        }
    }

    /**
     * Handles recognition error from service.
     */
    private fun handleRecognitionError(error: String) {
        viewModelScope.launch {
            _effect.send(VoiceCommandEffect.ShowError(error))
        }

        _state.update {
            it.copy(
                isListening = false,
                isParsing = false
            )
        }
    }

    /**
     * Handles permission denied.
     */
    private fun handlePermissionDenied() {
        _state.update {
            it.copy(
                isListening = false,
                permissionDenied = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}

/**
 * UI state for Voice Command screen.
 */
data class VoiceCommandState(
    val isListening: Boolean = false,
    val isParsing: Boolean = false,
    val permissionDenied: Boolean = false,
    val recognizedText: String = ""
)

/**
 * Events from UI to ViewModel.
 */
sealed class VoiceCommandEvent {
    object StartListening : VoiceCommandEvent()
    object StopListening : VoiceCommandEvent()
    data class RecognitionResult(val text: String) : VoiceCommandEvent()
    data class RecognitionError(val error: String) : VoiceCommandEvent()
    object PermissionDenied : VoiceCommandEvent()
}

/**
 * One-time effects from ViewModel to UI.
 */
sealed class VoiceCommandEffect {
    data class ShowMessage(val message: String) : VoiceCommandEffect()
    data class ShowError(val message: String) : VoiceCommandEffect()
    object NavigateToAlarmEdit : VoiceCommandEffect()
    object NavigateToTimer : VoiceCommandEffect()
}
