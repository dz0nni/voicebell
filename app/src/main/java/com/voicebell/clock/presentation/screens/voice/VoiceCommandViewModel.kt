package com.voicebell.clock.presentation.screens.voice

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.usecase.timer.GetActiveTimerUseCase
import com.voicebell.clock.domain.usecase.voice.ExecuteVoiceCommandUseCase
import com.voicebell.clock.domain.usecase.voice.CommandExecutionResult
import com.voicebell.clock.util.VoiceCommandParser
import com.voicebell.clock.util.VoiceCommandResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

/**
 * ViewModel for voice command screen.
 *
 * Handles:
 * - Voice recognition state
 * - Command execution
 * - Result display
 * - Active timer monitoring
 * - State persistence (survives navigation)
 */
@HiltViewModel
class VoiceCommandViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val executeVoiceCommandUseCase: ExecuteVoiceCommandUseCase,
    private val voiceCommandParser: VoiceCommandParser,
    private val getActiveTimerUseCase: GetActiveTimerUseCase
) : ViewModel() {

    companion object {
        private const val KEY_RESULT_TEXT = "result_text"
        private const val KEY_IS_SUCCESS = "is_success"
        private const val KEY_LOG_MESSAGES = "log_messages"
        private const val MAX_LOG_MESSAGES = 30
    }

    private val _state = MutableStateFlow(
        VoiceCommandState(
            resultText = savedStateHandle.get<String>(KEY_RESULT_TEXT) ?: "",
            isSuccess = savedStateHandle.get<Boolean>(KEY_IS_SUCCESS) ?: true,
            logMessages = savedStateHandle.get<List<LogMessage>>(KEY_LOG_MESSAGES) ?: emptyList()
        )
    )
    val state: StateFlow<VoiceCommandState> = _state.asStateFlow()

    init {
        // Observe active timer for countdown display
        getActiveTimerUseCase()
            .onEach { activeTimers ->
                val activeTimer = activeTimers.firstOrNull()
                _state.update { it.copy(activeTimer = activeTimer) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: VoiceCommandEvent) {
        android.util.Log.d("VoiceCommandViewModel", "Received event: $event")
        when (event) {
            is VoiceCommandEvent.StartListening -> {
                updateState {
                    it.copy(
                        isListening = true,
                        resultText = ""
                    )
                }
                addLog("🎤 Started listening...", LogType.INFO)
            }
            is VoiceCommandEvent.StopListening -> {
                updateState { it.copy(isListening = false) }
                addLog("⏸️ Stopped listening", LogType.INFO)
                addLog("🔄 Processing audio...", LogType.PROCESSING)
            }
            is VoiceCommandEvent.RecognitionResult -> {
                addLog("🎤 Speech recognized: \"${event.text}\"", LogType.SUCCESS)
                addLog("⚙️ Processing command...", LogType.PROCESSING)
                executeCommand(event.text)
            }
            is VoiceCommandEvent.ShowError -> {
                updateState {
                    it.copy(
                        isListening = false,
                        resultText = event.message,
                        isSuccess = false
                    )
                }
                addLog("❌ Error: ${event.message}", LogType.ERROR)
            }
        }
    }

    private fun addLog(message: String, type: LogType = LogType.INFO) {
        _state.update {
            val newLogs = (it.logMessages + LogMessage(message = message, type = type))
                .takeLast(MAX_LOG_MESSAGES) // Keep only last 30 messages

            // Save to savedStateHandle
            savedStateHandle[KEY_LOG_MESSAGES] = newLogs

            it.copy(logMessages = newLogs)
        }
    }

    private fun updateState(update: (VoiceCommandState) -> VoiceCommandState) {
        _state.update { currentState ->
            val newState = update(currentState)

            // Save critical state to savedStateHandle
            savedStateHandle[KEY_RESULT_TEXT] = newState.resultText
            savedStateHandle[KEY_IS_SUCCESS] = newState.isSuccess
            savedStateHandle[KEY_LOG_MESSAGES] = newState.logMessages.takeLast(MAX_LOG_MESSAGES)

            newState
        }
    }

    private fun executeCommand(recognizedText: String) {
        viewModelScope.launch {
            updateState { it.copy(isListening = false) }

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
                    updateState {
                        it.copy(
                            resultText = result.message,
                            isSuccess = true
                        )
                    }
                }
                is CommandExecutionResult.Error -> {
                    addLog("❌ ${result.message}", LogType.ERROR)
                    updateState {
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
    val logMessages: List<LogMessage> = emptyList(),
    val activeTimer: com.voicebell.clock.domain.model.Timer? = null
)

/**
 * Log message for debugging voice recognition process.
 */
@Parcelize
data class LogMessage(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
) : Parcelable

@Parcelize
enum class LogType : Parcelable {
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
