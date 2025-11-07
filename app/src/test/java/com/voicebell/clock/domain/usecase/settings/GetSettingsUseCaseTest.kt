package com.voicebell.clock.domain.usecase.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.ThemeMode
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetSettingsUseCase.
 */
class GetSettingsUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetSettingsUseCase

    private val testSettings = Settings(
        uiMode = UiMode.CLASSIC,
        themeMode = ThemeMode.SYSTEM,
        use24HourFormat = true,
        defaultSnoozeDuration = 10,
        defaultAlarmVolume = 80,
        defaultVibrateEnabled = true,
        defaultGradualVolumeEnabled = true,
        voiceCommandEnabled = true,
        maxRecentAlarms = 3,
        maxRecentTimers = 3
    )

    @Before
    fun setup() {
        settingsRepository = mockk()
        useCase = GetSettingsUseCase(settingsRepository)
    }

    @Test
    fun `invoke should return settings flow from repository`() = runTest {
        // Given
        every { settingsRepository.getSettingsFlow() } returns flowOf(testSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings).isEqualTo(testSettings)
            awaitComplete()
        }
        verify { settingsRepository.getSettingsFlow() }
    }

    @Test
    fun `invoke should return settings with all properties`() = runTest {
        // Given
        every { settingsRepository.getSettingsFlow() } returns flowOf(testSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.uiMode).isEqualTo(UiMode.CLASSIC)
            assertThat(settings.themeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(settings.use24HourFormat).isTrue()
            assertThat(settings.defaultSnoozeDuration).isEqualTo(10)
            assertThat(settings.defaultAlarmVolume).isEqualTo(80)
            assertThat(settings.defaultVibrateEnabled).isTrue()
            assertThat(settings.defaultGradualVolumeEnabled).isTrue()
            assertThat(settings.voiceCommandEnabled).isTrue()
            assertThat(settings.maxRecentAlarms).isEqualTo(3)
            assertThat(settings.maxRecentTimers).isEqualTo(3)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return experimental UI mode settings`() = runTest {
        // Given
        val experimentalSettings = testSettings.copy(uiMode = UiMode.EXPERIMENTAL)
        every { settingsRepository.getSettingsFlow() } returns flowOf(experimentalSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.uiMode).isEqualTo(UiMode.EXPERIMENTAL)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return dark theme settings`() = runTest {
        // Given
        val darkSettings = testSettings.copy(themeMode = ThemeMode.DARK)
        every { settingsRepository.getSettingsFlow() } returns flowOf(darkSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.themeMode).isEqualTo(ThemeMode.DARK)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return light theme settings`() = runTest {
        // Given
        val lightSettings = testSettings.copy(themeMode = ThemeMode.LIGHT)
        every { settingsRepository.getSettingsFlow() } returns flowOf(lightSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.themeMode).isEqualTo(ThemeMode.LIGHT)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return 12-hour format settings`() = runTest {
        // Given
        val settings12Hour = testSettings.copy(use24HourFormat = false)
        every { settingsRepository.getSettingsFlow() } returns flowOf(settings12Hour)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.use24HourFormat).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return settings with voice commands disabled`() = runTest {
        // Given
        val settingsNoVoice = testSettings.copy(voiceCommandEnabled = false)
        every { settingsRepository.getSettingsFlow() } returns flowOf(settingsNoVoice)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.voiceCommandEnabled).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return settings with custom snooze duration`() = runTest {
        // Given
        val customSettings = testSettings.copy(defaultSnoozeDuration = 15)
        every { settingsRepository.getSettingsFlow() } returns flowOf(customSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.defaultSnoozeDuration).isEqualTo(15)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return settings with custom alarm volume`() = runTest {
        // Given
        val customSettings = testSettings.copy(defaultAlarmVolume = 50)
        every { settingsRepository.getSettingsFlow() } returns flowOf(customSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.defaultAlarmVolume).isEqualTo(50)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return settings with vibrate disabled`() = runTest {
        // Given
        val noVibrateSettings = testSettings.copy(defaultVibrateEnabled = false)
        every { settingsRepository.getSettingsFlow() } returns flowOf(noVibrateSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.defaultVibrateEnabled).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return settings with gradual volume disabled`() = runTest {
        // Given
        val noGradualSettings = testSettings.copy(defaultGradualVolumeEnabled = false)
        every { settingsRepository.getSettingsFlow() } returns flowOf(noGradualSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.defaultGradualVolumeEnabled).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return settings with custom max recent alarms`() = runTest {
        // Given
        val customSettings = testSettings.copy(maxRecentAlarms = 5)
        every { settingsRepository.getSettingsFlow() } returns flowOf(customSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.maxRecentAlarms).isEqualTo(5)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return settings with custom max recent timers`() = runTest {
        // Given
        val customSettings = testSettings.copy(maxRecentTimers = 7)
        every { settingsRepository.getSettingsFlow() } returns flowOf(customSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.maxRecentTimers).isEqualTo(7)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should emit updated settings when they change`() = runTest {
        // Given - settings change during observation
        val updatedSettings = testSettings.copy(
            uiMode = UiMode.EXPERIMENTAL,
            themeMode = ThemeMode.DARK,
            use24HourFormat = false
        )
        every { settingsRepository.getSettingsFlow() } returns flowOf(testSettings, updatedSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings1 = awaitItem()
            assertThat(settings1.uiMode).isEqualTo(UiMode.CLASSIC)
            assertThat(settings1.themeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(settings1.use24HourFormat).isTrue()

            val settings2 = awaitItem()
            assertThat(settings2.uiMode).isEqualTo(UiMode.EXPERIMENTAL)
            assertThat(settings2.themeMode).isEqualTo(ThemeMode.DARK)
            assertThat(settings2.use24HourFormat).isFalse()

            awaitComplete()
        }
    }

    @Test
    fun `invoke should emit multiple settings changes`() = runTest {
        // Given
        val settings1 = testSettings
        val settings2 = testSettings.copy(defaultAlarmVolume = 50)
        val settings3 = testSettings.copy(defaultAlarmVolume = 30)
        every { settingsRepository.getSettingsFlow() } returns flowOf(settings1, settings2, settings3)

        // When
        val result = useCase()

        // Then
        result.test {
            assertThat(awaitItem().defaultAlarmVolume).isEqualTo(80)
            assertThat(awaitItem().defaultAlarmVolume).isEqualTo(50)
            assertThat(awaitItem().defaultAlarmVolume).isEqualTo(30)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return default settings`() = runTest {
        // Given
        val defaultSettings = Settings()
        every { settingsRepository.getSettingsFlow() } returns flowOf(defaultSettings)

        // When
        val result = useCase()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.uiMode).isEqualTo(UiMode.CLASSIC)
            assertThat(settings.themeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(settings.use24HourFormat).isTrue()
            assertThat(settings.voiceCommandEnabled).isTrue()
            awaitComplete()
        }
    }
}
