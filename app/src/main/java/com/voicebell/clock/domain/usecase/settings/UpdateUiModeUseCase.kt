package com.voicebell.clock.domain.usecase.settings

import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating UI mode (Classic or Experimental).
 */
class UpdateUiModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(uiMode: UiMode): Result<Unit> {
        return try {
            settingsRepository.updateUiMode(uiMode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
