package com.voicebell.clock.domain.repository

import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.ThemeMode
import com.voicebell.clock.domain.model.UiMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings.
 */
interface SettingsRepository {
    /**
     * Get settings as a Flow for reactive updates
     */
    fun getSettingsFlow(): Flow<Settings>

    /**
     * Get current settings
     */
    suspend fun getSettings(): Settings

    /**
     * Update all settings
     */
    suspend fun updateSettings(settings: Settings)

    /**
     * Update UI mode (Classic or Experimental)
     */
    suspend fun updateUiMode(uiMode: UiMode)

    /**
     * Update theme mode (Light, Dark, System)
     */
    suspend fun updateThemeMode(themeMode: ThemeMode)

    /**
     * Update 24-hour format preference
     */
    suspend fun updateUse24HourFormat(use24Hour: Boolean)

    /**
     * Update voice command enabled
     */
    suspend fun updateVoiceCommandEnabled(enabled: Boolean)

    /**
     * Update auto-delete finished timer setting
     */
    suspend fun updateAutoDeleteFinishedTimer(enabled: Boolean)

    /**
     * Update play timer sound only to Bluetooth headphones setting
     */
    suspend fun updatePlayTimerSoundOnlyToBluetooth(enabled: Boolean)

    /**
     * Initialize settings with defaults (called on first launch)
     */
    suspend fun initializeDefaults()
}
