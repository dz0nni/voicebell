package com.voicebell.clock.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for application settings.
 * Single-row table with id=1 for app-wide preferences.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    /**
     * UI mode: CLASSIC or EXPERIMENTAL
     */
    val uiMode: String = "EXPERIMENTAL",

    /**
     * Theme mode: LIGHT, DARK, or SYSTEM
     */
    val themeMode: String = "SYSTEM",

    /**
     * Whether to use 24-hour time format
     */
    val use24HourFormat: Boolean = true,

    /**
     * Default snooze duration in minutes
     */
    val defaultSnoozeDuration: Int = 10,

    /**
     * Default alarm volume level (0-100)
     */
    val defaultAlarmVolume: Int = 80,

    /**
     * Whether to enable vibration by default for new alarms
     */
    val defaultVibrateEnabled: Boolean = true,

    /**
     * Whether to enable gradual volume increase by default
     */
    val defaultGradualVolumeEnabled: Boolean = true,

    /**
     * Whether voice command feature is enabled
     */
    val voiceCommandEnabled: Boolean = true,

    /**
     * Maximum number of recent alarms to show in experimental view
     */
    val maxRecentAlarms: Int = 3,

    /**
     * Maximum number of recent timers to show in experimental view
     */
    val maxRecentTimers: Int = 3,

    /**
     * Whether to automatically delete finished timers
     */
    val autoDeleteFinishedTimer: Boolean = false
)
