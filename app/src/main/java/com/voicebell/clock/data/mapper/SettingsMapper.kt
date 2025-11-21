package com.voicebell.clock.data.mapper

import com.voicebell.clock.data.local.database.entities.SettingsEntity
import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.ThemeMode
import com.voicebell.clock.domain.model.UiMode

/**
 * Convert SettingsEntity to Settings domain model
 */
fun SettingsEntity.toDomain(): Settings {
    return Settings(
        uiMode = UiMode.fromString(uiMode),
        themeMode = ThemeMode.fromString(themeMode),
        use24HourFormat = use24HourFormat,
        defaultSnoozeDuration = defaultSnoozeDuration,
        defaultAlarmVolume = defaultAlarmVolume,
        defaultVibrateEnabled = defaultVibrateEnabled,
        defaultGradualVolumeEnabled = defaultGradualVolumeEnabled,
        voiceCommandEnabled = voiceCommandEnabled,
        maxRecentAlarms = maxRecentAlarms,
        maxRecentTimers = maxRecentTimers,
        autoDeleteFinishedTimer = autoDeleteFinishedTimer,
        playTimerSoundOnlyToBluetooth = playTimerSoundOnlyToBluetooth
    )
}

/**
 * Convert Settings domain model to SettingsEntity
 */
fun Settings.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = 1, // Always use id=1 for single-row settings
        uiMode = uiMode.name,
        themeMode = themeMode.name,
        use24HourFormat = use24HourFormat,
        defaultSnoozeDuration = defaultSnoozeDuration,
        defaultAlarmVolume = defaultAlarmVolume,
        defaultVibrateEnabled = defaultVibrateEnabled,
        defaultGradualVolumeEnabled = defaultGradualVolumeEnabled,
        voiceCommandEnabled = voiceCommandEnabled,
        maxRecentAlarms = maxRecentAlarms,
        maxRecentTimers = maxRecentTimers,
        autoDeleteFinishedTimer = autoDeleteFinishedTimer,
        playTimerSoundOnlyToBluetooth = playTimerSoundOnlyToBluetooth
    )
}
