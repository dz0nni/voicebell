package com.voicebell.clock.presentation.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.usecase.voice.ExecuteVoiceCommandUseCase
import com.voicebell.clock.domain.usecase.voice.CommandExecutionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for voice command screen.
 *
 * Handles:
 * - Voice recognition state
 * - Command execution
 * - Result display
 */
@HiltViewModel
class VoiceCommandViewModel @Inject constructor(
    private val executeVoiceCommandUseCase: ExecuteVoiceCommandUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceCommandState())
    val state: StateFlow<VoiceCommandState> = _state.asStateFlow()

    fun onEvent(event: VoiceCommandEvent) {
        when (event) {
            is VoiceCommandEvent.StartListening -> {
                _state.update { it.copy(isListening = true, resultText = "") }
            }
            is VoiceCommandEvent.StopListening -> {
                _state.update { it.copy(isListening = false) }
            }
            is VoiceCommandEvent.RecognitionResult -> {
                executeCommand(event.text)
            }
            is VoiceCommandEvent.ShowError -> {
                _state.update {
                    it.copy(
                        isListening = false,
                        resultText = event.message,
                        isSuccess = false
                    )
                }
            }
        }
    }

    private fun executeCommand(recognizedText: String) {
        viewModelScope.launch {
            _state.update { it.copy(isListening = false) }

            val result = executeVoiceCommandUseCase(recognizedText)

            when (result) {
                is CommandExecutionResult.Success -> {
                    _state.update {
                        it.copy(
                            resultText = result.message,
                            isSuccess = true
                        )
                    }
                }
                is CommandExecutionResult.Error -> {
                    _state.update {
                        it.copy(
                            resultText = result.message,
                            isSuccess = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * State for voice command screen.
 */
data class VoiceCommandState(
    val isListening: Boolean = false,
    val resultText: String = "",
    val isSuccess: Boolean = true
)

/**
 * Events for voice command screen.
 */
sealed class VoiceCommandEvent {
    object StartListening : VoiceCommandEvent()
    object StopListening : VoiceCommandEvent()
    data class RecognitionResult(val text: String) : VoiceCommandEvent()
    data class ShowError(val message: String) : VoiceCommandEvent()
}
