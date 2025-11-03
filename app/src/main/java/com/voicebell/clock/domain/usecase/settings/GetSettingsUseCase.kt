package com.voicebell.clock.domain.usecase.settings

import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting app settings as a Flow.
 * Provides reactive updates when settings change.
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> {
        return settingsRepository.getSettingsFlow()
    }
}
