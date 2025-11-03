package com.voicebell.clock.presentation.navigation

/**
 * Navigation routes for VoiceBell app.
 */
sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Alarms : Routes("alarms")
    object AlarmEdit : Routes("alarm_edit/{alarmId}") {
        fun createRoute(alarmId: Long = 0L) = "alarm_edit/$alarmId"
    }
    object Timer : Routes("timer")
    object Stopwatch : Routes("stopwatch")
    object WorldClocks : Routes("world_clocks")
    object Settings : Routes("settings")
    object VoiceCommand : Routes("voice_command")
}
