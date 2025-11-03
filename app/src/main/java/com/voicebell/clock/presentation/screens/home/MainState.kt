package com.voicebell.clock.presentation.screens.home

import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.Timer

/**
 * UI state for main screen
 */
data class MainState(
    val settings: Settings = Settings(),
    val recentAlarms: List<Alarm> = emptyList(),
    val recentTimers: List<Timer> = emptyList()
)
