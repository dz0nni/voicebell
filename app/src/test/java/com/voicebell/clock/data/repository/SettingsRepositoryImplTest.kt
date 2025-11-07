package com.voicebell.clock.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.data.local.database.dao.SettingsDao
import com.voicebell.clock.data.local.database.entities.SettingsEntity
import com.voicebell.clock.domain.model.Settings
import com.voicebell.clock.domain.model.ThemeMode
import com.voicebell.clock.domain.model.UiMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsRepositoryImpl.
 */
class SettingsRepositoryImplTest {

    private lateinit var settingsDao: SettingsDao
    private lateinit var repository: SettingsRepositoryImpl

    private val testSettingsEntity = SettingsEntity(
        id = 1,
        uiMode = "CLASSIC",
        themeMode = "SYSTEM",
        use24HourFormat = true,
        defaultSnoozeDuration = 10,
        defaultAlarmVolume = 80,
        defaultVibrateEnabled = true,
        defaultGradualVolumeEnabled = true,
        voiceCommandEnabled = true,
        maxRecentAlarms = 3,
        maxRecentTimers = 3
    )

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
        settingsDao = mockk()
        repository = SettingsRepositoryImpl(settingsDao)
    }

    // ============================================
    // GET SETTINGS FLOW TESTS
    // ============================================

    @Test
    fun `getSettingsFlow should return mapped settings`() = runTest {
        // Given
        every { settingsDao.getSettingsFlow() } returns flowOf(testSettingsEntity)

        // When
        val result = repository.getSettingsFlow()

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
    fun `getSettingsFlow should return default settings when entity is null`() = runTest {
        // Given
        every { settingsDao.getSettingsFlow() } returns flowOf(null)

        // When
        val result = repository.getSettingsFlow()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings).isEqualTo(Settings())
            awaitComplete()
        }
    }

    @Test
    fun `getSettingsFlow should map experimental UI mode correctly`() = runTest {
        // Given
        val experimentalEntity = testSettingsEntity.copy(uiMode = "EXPERIMENTAL")
        every { settingsDao.getSettingsFlow() } returns flowOf(experimentalEntity)

        // When
        val result = repository.getSettingsFlow()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.uiMode).isEqualTo(UiMode.EXPERIMENTAL)
            awaitComplete()
        }
    }

    @Test
    fun `getSettingsFlow should map dark theme mode correctly`() = runTest {
        // Given
        val darkThemeEntity = testSettingsEntity.copy(themeMode = "DARK")
        every { settingsDao.getSettingsFlow() } returns flowOf(darkThemeEntity)

        // When
        val result = repository.getSettingsFlow()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.themeMode).isEqualTo(ThemeMode.DARK)
            awaitComplete()
        }
    }

    @Test
    fun `getSettingsFlow should map light theme mode correctly`() = runTest {
        // Given
        val lightThemeEntity = testSettingsEntity.copy(themeMode = "LIGHT")
        every { settingsDao.getSettingsFlow() } returns flowOf(lightThemeEntity)

        // When
        val result = repository.getSettingsFlow()

        // Then
        result.test {
            val settings = awaitItem()
            assertThat(settings.themeMode).isEqualTo(ThemeMode.LIGHT)
            awaitComplete()
        }
    }

    @Test
    fun `getSettingsFlow should emit updated settings`() = runTest {
        // Given - settings change during observation
        val updatedEntity = testSettingsEntity.copy(
            uiMode = "EXPERIMENTAL",
            use24HourFormat = false
        )
        every { settingsDao.getSettingsFlow() } returns flowOf(testSettingsEntity, updatedEntity)

        // When
        val result = repository.getSettingsFlow()

        // Then
        result.test {
            val settings1 = awaitItem()
            assertThat(settings1.uiMode).isEqualTo(UiMode.CLASSIC)
            assertThat(settings1.use24HourFormat).isTrue()

            val settings2 = awaitItem()
            assertThat(settings2.uiMode).isEqualTo(UiMode.EXPERIMENTAL)
            assertThat(settings2.use24HourFormat).isFalse()

            awaitComplete()
        }
    }

    // ============================================
    // GET SETTINGS (SINGLE READ) TESTS
    // ============================================

    @Test
    fun `getSettings should return mapped settings`() = runTest {
        // Given
        coEvery { settingsDao.getSettings() } returns testSettingsEntity

        // When
        val result = repository.getSettings()

        // Then
        assertThat(result.uiMode).isEqualTo(UiMode.CLASSIC)
        assertThat(result.themeMode).isEqualTo(ThemeMode.SYSTEM)
        assertThat(result.use24HourFormat).isTrue()
        coVerify { settingsDao.getSettings() }
    }

    @Test
    fun `getSettings should return default settings when entity is null`() = runTest {
        // Given
        coEvery { settingsDao.getSettings() } returns null

        // When
        val result = repository.getSettings()

        // Then
        assertThat(result).isEqualTo(Settings())
    }

    @Test
    fun `getSettings should return settings with custom values`() = runTest {
        // Given
        val customEntity = testSettingsEntity.copy(
            defaultSnoozeDuration = 15,
            defaultAlarmVolume = 50,
            maxRecentAlarms = 5,
            maxRecentTimers = 7
        )
        coEvery { settingsDao.getSettings() } returns customEntity

        // When
        val result = repository.getSettings()

        // Then
        assertThat(result.defaultSnoozeDuration).isEqualTo(15)
        assertThat(result.defaultAlarmVolume).isEqualTo(50)
        assertThat(result.maxRecentAlarms).isEqualTo(5)
        assertThat(result.maxRecentTimers).isEqualTo(7)
    }

    // ============================================
    // UPDATE SETTINGS TESTS
    // ============================================

    @Test
    fun `updateSettings should map and update entity`() = runTest {
        // Given
        coEvery { settingsDao.update(any()) } returns Unit

        // When
        repository.updateSettings(testSettings)

        // Then
        coVerify {
            settingsDao.update(match { entity ->
                entity.id == 1 &&
                entity.uiMode == "CLASSIC" &&
                entity.themeMode == "SYSTEM" &&
                entity.use24HourFormat == true
            })
        }
    }

    @Test
    fun `updateSettings should update experimental UI mode`() = runTest {
        // Given
        val experimentalSettings = testSettings.copy(uiMode = UiMode.EXPERIMENTAL)
        coEvery { settingsDao.update(any()) } returns Unit

        // When
        repository.updateSettings(experimentalSettings)

        // Then
        coVerify {
            settingsDao.update(match { entity ->
                entity.uiMode == "EXPERIMENTAL"
            })
        }
    }

    @Test
    fun `updateSettings should update dark theme`() = runTest {
        // Given
        val darkSettings = testSettings.copy(themeMode = ThemeMode.DARK)
        coEvery { settingsDao.update(any()) } returns Unit

        // When
        repository.updateSettings(darkSettings)

        // Then
        coVerify {
            settingsDao.update(match { entity ->
                entity.themeMode == "DARK"
            })
        }
    }

    @Test
    fun `updateSettings should update 12-hour format`() = runTest {
        // Given
        val settings12Hour = testSettings.copy(use24HourFormat = false)
        coEvery { settingsDao.update(any()) } returns Unit

        // When
        repository.updateSettings(settings12Hour)

        // Then
        coVerify {
            settingsDao.update(match { entity ->
                entity.use24HourFormat == false
            })
        }
    }

    @Test
    fun `updateSettings should update voice command disabled`() = runTest {
        // Given
        val settingsNoVoice = testSettings.copy(voiceCommandEnabled = false)
        coEvery { settingsDao.update(any()) } returns Unit

        // When
        repository.updateSettings(settingsNoVoice)

        // Then
        coVerify {
            settingsDao.update(match { entity ->
                entity.voiceCommandEnabled == false
            })
        }
    }

    // ============================================
    // UPDATE INDIVIDUAL FIELDS TESTS
    // ============================================

    @Test
    fun `updateUiMode should update UI mode to experimental`() = runTest {
        // Given
        coEvery { settingsDao.updateUiMode(any()) } returns Unit

        // When
        repository.updateUiMode(UiMode.EXPERIMENTAL)

        // Then
        coVerify { settingsDao.updateUiMode("EXPERIMENTAL") }
    }

    @Test
    fun `updateUiMode should update UI mode to classic`() = runTest {
        // Given
        coEvery { settingsDao.updateUiMode(any()) } returns Unit

        // When
        repository.updateUiMode(UiMode.CLASSIC)

        // Then
        coVerify { settingsDao.updateUiMode("CLASSIC") }
    }

    @Test
    fun `updateThemeMode should update to dark theme`() = runTest {
        // Given
        coEvery { settingsDao.updateThemeMode(any()) } returns Unit

        // When
        repository.updateThemeMode(ThemeMode.DARK)

        // Then
        coVerify { settingsDao.updateThemeMode("DARK") }
    }

    @Test
    fun `updateThemeMode should update to light theme`() = runTest {
        // Given
        coEvery { settingsDao.updateThemeMode(any()) } returns Unit

        // When
        repository.updateThemeMode(ThemeMode.LIGHT)

        // Then
        coVerify { settingsDao.updateThemeMode("LIGHT") }
    }

    @Test
    fun `updateThemeMode should update to system theme`() = runTest {
        // Given
        coEvery { settingsDao.updateThemeMode(any()) } returns Unit

        // When
        repository.updateThemeMode(ThemeMode.SYSTEM)

        // Then
        coVerify { settingsDao.updateThemeMode("SYSTEM") }
    }

    @Test
    fun `updateUse24HourFormat should enable 24-hour format`() = runTest {
        // Given
        coEvery { settingsDao.updateUse24HourFormat(any()) } returns Unit

        // When
        repository.updateUse24HourFormat(true)

        // Then
        coVerify { settingsDao.updateUse24HourFormat(true) }
    }

    @Test
    fun `updateUse24HourFormat should disable 24-hour format`() = runTest {
        // Given
        coEvery { settingsDao.updateUse24HourFormat(any()) } returns Unit

        // When
        repository.updateUse24HourFormat(false)

        // Then
        coVerify { settingsDao.updateUse24HourFormat(false) }
    }

    @Test
    fun `updateVoiceCommandEnabled should enable voice commands`() = runTest {
        // Given
        coEvery { settingsDao.updateVoiceCommandEnabled(any()) } returns Unit

        // When
        repository.updateVoiceCommandEnabled(true)

        // Then
        coVerify { settingsDao.updateVoiceCommandEnabled(true) }
    }

    @Test
    fun `updateVoiceCommandEnabled should disable voice commands`() = runTest {
        // Given
        coEvery { settingsDao.updateVoiceCommandEnabled(any()) } returns Unit

        // When
        repository.updateVoiceCommandEnabled(false)

        // Then
        coVerify { settingsDao.updateVoiceCommandEnabled(false) }
    }

    // ============================================
    // INITIALIZE DEFAULTS TESTS
    // ============================================

    @Test
    fun `initializeDefaults should insert default settings when none exist`() = runTest {
        // Given
        coEvery { settingsDao.getSettings() } returns null
        coEvery { settingsDao.insert(any()) } returns Unit

        // When
        repository.initializeDefaults()

        // Then
        coVerify { settingsDao.insert(any()) }
    }

    @Test
    fun `initializeDefaults should not insert when settings already exist`() = runTest {
        // Given
        coEvery { settingsDao.getSettings() } returns testSettingsEntity

        // When
        repository.initializeDefaults()

        // Then
        coVerify(exactly = 0) { settingsDao.insert(any()) }
    }

    @Test
    fun `initializeDefaults should insert entity with default values`() = runTest {
        // Given
        coEvery { settingsDao.getSettings() } returns null
        coEvery { settingsDao.insert(any()) } returns Unit

        // When
        repository.initializeDefaults()

        // Then
        coVerify {
            settingsDao.insert(match { entity ->
                entity.id == 1 &&
                entity.uiMode == "CLASSIC" &&
                entity.themeMode == "SYSTEM" &&
                entity.use24HourFormat == true &&
                entity.voiceCommandEnabled == true
            })
        }
    }
}
