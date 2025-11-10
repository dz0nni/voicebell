package com.voicebell.clock.presentation.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.usecase.voice.ExecuteVoiceCommandUseCase
import com.voicebell.clock.domain.usecase.voice.CommandExecutionResult
import com.voicebell.clock.util.VoiceCommandParser
import com.voicebell.clock.util.VoiceCommandResult
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
    private val executeVoiceCommandUseCase: ExecuteVoiceCommandUseCase,
    private val voiceCommandParser: VoiceCommandParser
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceCommandState())
    val state: StateFlow<VoiceCommandState> = _state.asStateFlow()

    fun onEvent(event: VoiceCommandEvent) {
        when (event) {
            is VoiceCommandEvent.StartListening -> {
                _state.update {
                    it.copy(
                        isListening = true,
                        resultText = "",
                        logMessages = it.logMessages + LogMessage(
                            message = "🎤 Started listening...",
                            type = LogType.INFO
                        )
                    )
                }
            }
            is VoiceCommandEvent.StopListening -> {
                _state.update {
                    it.copy(
                        isListening = false,
                        logMessages = it.logMessages + LogMessage(
                            message = "⏸️ Stopped listening",
                            type = LogType.INFO
                        )
                    )
                }
            }
            is VoiceCommandEvent.RecognitionResult -> {
                addLog("✅ Recognized: \"${event.text}\"", LogType.SUCCESS)
                executeCommand(event.text)
            }
            is VoiceCommandEvent.ShowError -> {
                _state.update {
                    it.copy(
                        isListening = false,
                        resultText = event.message,
                        isSuccess = false,
                        logMessages = it.logMessages + LogMessage(
                            message = "❌ Error: ${event.message}",
                            type = LogType.ERROR
                        )
                    )
                }
            }
        }
    }

    private fun addLog(message: String, type: LogType = LogType.INFO) {
        _state.update {
            it.copy(logMessages = it.logMessages + LogMessage(message = message, type = type))
        }
    }

    private fun executeCommand(recognizedText: String) {
        viewModelScope.launch {
            _state.update { it.copy(isListening = false) }

            addLog("🔍 Parsing command...", LogType.PROCESSING)

            // Parse command and log details
            val parseResult = voiceCommandParser.parseCommand(recognizedText)

            when (parseResult) {
                is VoiceCommandResult.AlarmCommand -> {
                    addLog("⏰ Detected: Alarm command", LogType.INFO)
                    addLog("🕐 Time: ${parseResult.time}", LogType.INFO)
                    if (parseResult.label != null) {
                        addLog("🏷️ Label: ${parseResult.label}", LogType.INFO)
                    }
                    addLog("💾 Creating alarm...", LogType.PROCESSING)
                }
                is VoiceCommandResult.TimerCommand -> {
                    addLog("⏱️ Detected: Timer command", LogType.INFO)
                    val minutes = (parseResult.durationMillis / 1000 / 60).toInt()
                    val seconds = ((parseResult.durationMillis / 1000) % 60).toInt()
                    if (minutes > 0) {
                        addLog("⏲️ Duration: ${minutes}min ${seconds}s", LogType.INFO)
                    } else {
                        addLog("⏲️ Duration: ${seconds}s", LogType.INFO)
                    }
                    if (parseResult.label != null) {
                        addLog("🏷️ Label: ${parseResult.label}", LogType.INFO)
                    }
                    addLog("💾 Starting timer...", LogType.PROCESSING)
                }
                is VoiceCommandResult.Unknown -> {
                    addLog("❓ Unknown command type", LogType.ERROR)
                }
                is VoiceCommandResult.Error -> {
                    addLog("❌ Parse error: ${parseResult.message}", LogType.ERROR)
                }
            }

            // Execute command
            val result = executeVoiceCommandUseCase(recognizedText)

            when (result) {
                is CommandExecutionResult.Success -> {
                    addLog("✅ ${result.message}", LogType.SUCCESS)
                    _state.update {
                        it.copy(
                            resultText = result.message,
                            isSuccess = true
                        )
                    }
                }
                is CommandExecutionResult.Error -> {
                    addLog("❌ ${result.message}", LogType.ERROR)
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
    val isSuccess: Boolean = true,
    val logMessages: List<LogMessage> = emptyList()
)

/**
 * Log message for debugging voice recognition process.
 */
data class LogMessage(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO,     // General info (blue/gray)
    SUCCESS,  // Success step (green)
    ERROR,    // Error step (red)
    PROCESSING // Processing step (yellow/orange)
}

/**
 * Events for voice command screen.
 */
sealed class VoiceCommandEvent {
    object StartListening : VoiceCommandEvent()
    object StopListening : VoiceCommandEvent()
    data class RecognitionResult(val text: String) : VoiceCommandEvent()
    data class ShowError(val message: String) : VoiceCommandEvent()
}
