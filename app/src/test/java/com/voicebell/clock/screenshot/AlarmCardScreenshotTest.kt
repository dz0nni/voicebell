package com.voicebell.clock.screenshot

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.voicebell.clock.domain.model.alarm.Alarm
import com.voicebell.clock.domain.model.alarm.AlarmTone
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

/**
 * Example Paparazzi screenshot test for Alarm UI components.
 *
 * Paparazzi allows us to test Compose UIs without running on a device/emulator.
 * It takes screenshots of composables and compares them on subsequent runs.
 *
 * Usage:
 * - Run: ./gradlew recordPaparazziDebug  (first time - records golden images)
 * - Run: ./gradlew verifyPaparazziDebug  (compare against golden images)
 *
 * Note: This test is currently disabled because AlarmCard composable
 * needs to be extracted from AlarmListScreen for isolated testing.
 * See TODO in presentation/alarm/AlarmListScreen.kt
 */
class AlarmCardScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.DayNight.NoActionBar",
        showSystemUi = false
    )

    // Example test - disabled until AlarmCard is extracted
    /*
    @Test
    fun alarmCard_enabled_showsCorrectly() {
        paparazzi.snapshot {
            AlarmCard(
                alarm = createSampleAlarm(isEnabled = true),
                onToggle = {},
                onClick = {},
                onDelete = {}
            )
        }
    }

    @Test
    fun alarmCard_disabled_showsCorrectly() {
        paparazzi.snapshot {
            AlarmCard(
                alarm = createSampleAlarm(isEnabled = false),
                onToggle = {},
                onClick = {},
                onDelete = {}
            )
        }
    }

    @Test
    fun alarmCard_withRepeatDays_showsCorrectly() {
        paparazzi.snapshot {
            AlarmCard(
                alarm = createSampleAlarm(
                    isEnabled = true,
                    repeatDays = setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.FRIDAY
                    )
                ),
                onToggle = {},
                onClick = {},
                onDelete = {}
            )
        }
    }

    private fun createSampleAlarm(
        isEnabled: Boolean = true,
        repeatDays: Set<DayOfWeek> = emptySet()
    ): Alarm {
        return Alarm(
            id = 1,
            time = LocalTime.of(7, 30),
            label = "Morning Alarm",
            isEnabled = isEnabled,
            repeatDays = repeatDays,
            tone = AlarmTone.GENTLE,
            volume = 70,
            vibrate = true,
            snoozeEnabled = true,
            snoozeDuration = 5,
            maxSnoozeCount = 3,
            snoozeCount = 0,
            preAlarmCount = 1,
            preAlarmInterval = 30,
            gradualVolumeIncrease = true,
            gradualVolumeTime = 60,
            flashEnabled = false
        )
    }
    */

    @Test
    fun placeholder_paparazzi_configured() {
        // Placeholder test to verify Paparazzi is properly configured
        paparazzi.snapshot {
            SampleComposable()
        }
    }

    @Composable
    private fun SampleComposable() {
        androidx.compose.material3.Text("Paparazzi is configured!")
    }
}
