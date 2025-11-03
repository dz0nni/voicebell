package com.voicebell.clock.presentation.screens.settings

import com.voicebell.clock.domain.model.Settings

/**
 * UI state for settings screen
 */
data class SettingsState(
    val settings: Settings = Settings()
)
