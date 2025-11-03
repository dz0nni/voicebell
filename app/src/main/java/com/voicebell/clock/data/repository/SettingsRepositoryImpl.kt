package com.voicebell.clock.data.repository

import com.voicebell.clock.data.local.database.dao.SettingsDao
import com.voicebell.clock.data.local.database.entities.SettingsEntity
import com.voicebell.clock.data.mapper.toDomain
import com.voicebell.clock.data.mapper.toEntity
import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.ThemeMode
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository.
 * Handles reading and writing app settings to Room database.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getSettingsFlow(): Flow<Settings> {
        return settingsDao.getSettingsFlow()
            .map { entity ->
                entity?.toDomain() ?: Settings() // Return default settings if null
            }
    }

    override suspend fun getSettings(): Settings {
        val entity = settingsDao.getSettings()
        return entity?.toDomain() ?: Settings()
    }

    override suspend fun updateSettings(settings: Settings) {
        settingsDao.update(settings.toEntity())
    }

    override suspend fun updateUiMode(uiMode: UiMode) {
        settingsDao.updateUiMode(uiMode.name)
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        settingsDao.updateThemeMode(themeMode.name)
    }

    override suspend fun updateUse24HourFormat(use24Hour: Boolean) {
        settingsDao.updateUse24HourFormat(use24Hour)
    }

    override suspend fun updateVoiceCommandEnabled(enabled: Boolean) {
        settingsDao.updateVoiceCommandEnabled(enabled)
    }

    override suspend fun initializeDefaults() {
        // Check if settings already exist
        val existing = settingsDao.getSettings()
        if (existing == null) {
            // Insert default settings
            settingsDao.insert(SettingsEntity())
        }
    }
}
