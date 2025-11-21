package com.voicebell.clock.domain.model

/**
 * Domain model for application settings.
 * Represents user preferences for UI and alarm behavior.
 */
data class Settings(
    val uiMode: UiMode = UiMode.EXPERIMENTAL,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val use24HourFormat: Boolean = true,
    val defaultSnoozeDuration: Int = 10,
    val defaultAlarmVolume: Int = 80,
    val defaultVibrateEnabled: Boolean = true,
    val defaultGradualVolumeEnabled: Boolean = true,
    val voiceCommandEnabled: Boolean = true,
    val maxRecentAlarms: Int = 3,
    val maxRecentTimers: Int = 3,
    val autoDeleteFinishedTimer: Boolean = false
)

/**
 * Theme mode options
 */
enum class ThemeMode(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System default");

    companion object {
        fun fromString(value: String): ThemeMode {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}
