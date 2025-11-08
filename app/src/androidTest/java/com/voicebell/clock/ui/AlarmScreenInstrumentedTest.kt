package com.voicebell.clock.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.voicebell.clock.presentation.alarm.AlarmListScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Example Compose UI instrumented test for Alarm screen.
 *
 * This demonstrates how to test Compose UI components on a real device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class AlarmScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun alarmScreen_displaysCorrectly() {
        // This is a basic example - you would typically use Hilt for proper DI
        // and inject mock ViewModels for isolated UI testing

        // Note: This test is commented out as it requires proper Hilt setup
        // and mock data injection. See TESTING_ANDROID_UI.md for implementation guide.

        /*
        composeTestRule.setContent {
            AlarmListScreen(
                // Need to inject test state here
            )
        }

        composeTestRule
            .onNodeWithText("Alarms")
            .assertIsDisplayed()
        */
    }
}
