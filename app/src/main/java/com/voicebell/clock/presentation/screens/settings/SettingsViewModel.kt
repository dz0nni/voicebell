package com.voicebell.clock.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.domain.repository.SettingsRepository
import com.voicebell.clock.domain.usecase.settings.GetSettingsUseCase
import com.voicebell.clock.domain.usecase.settings.UpdateUiModeUseCase
import com.voicebell.clock.util.VoskModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateUiModeUseCase: UpdateUiModeUseCase,
    private val settingsRepository: SettingsRepository,
    private val voskModelManager: VoskModelManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
        checkVoiceModelStatus()
    }

    private fun loadSettings() {
        getSettingsUseCase()
            .onEach { settings ->
                _state.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    fun setUiMode(uiMode: UiMode) {
        viewModelScope.launch {
            updateUiModeUseCase(uiMode)
        }
    }

    fun toggleVoiceCommand() {
        viewModelScope.launch {
            val newValue = !_state.value.settings.voiceCommandEnabled
            settingsRepository.updateVoiceCommandEnabled(newValue)
        }
    }

    fun toggle24HourFormat() {
        viewModelScope.launch {
            val newValue = !_state.value.settings.use24HourFormat
            settingsRepository.updateUse24HourFormat(newValue)
        }
    }

    private fun checkVoiceModelStatus() {
        _state.update {
            it.copy(isVoiceModelDownloaded = voskModelManager.isModelDownloaded())
        }
    }

    fun downloadVoiceModel() {
        viewModelScope.launch {
            _state.update { it.copy(isDownloadingModel = true, modelDownloadProgress = 0f) }

            voskModelManager.extractModelFromAssets { progress ->
                _state.update { it.copy(modelDownloadProgress = progress) }
            }.onSuccess {
                _state.update {
                    it.copy(
                        isVoiceModelDownloaded = true,
                        isDownloadingModel = false,
                        modelDownloadProgress = 1f
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isDownloadingModel = false,
                        modelDownloadProgress = 0f
                    )
                }
                // TODO: Show error to user
            }
        }
    }

    fun deleteVoiceModel() {
        viewModelScope.launch {
            val success = voskModelManager.deleteModel()
            if (success) {
                _state.update { it.copy(isVoiceModelDownloaded = false) }
            }
        }
    }
}
