package com.voicebell.clock.domain.usecase.settings

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.UiMode
import com.voicebell.clock.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UpdateUiModeUseCase.
 */
class UpdateUiModeUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: UpdateUiModeUseCase

    @Before
    fun setup() {
        settingsRepository = mockk()
        useCase = UpdateUiModeUseCase(settingsRepository)
    }

    @Test
    fun `invoke should update UI mode to classic successfully`() = runTest {
        // Given
        coEvery { settingsRepository.updateUiMode(any()) } returns Unit

        // When
        val result = useCase(UiMode.CLASSIC)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { settingsRepository.updateUiMode(UiMode.CLASSIC) }
    }

    @Test
    fun `invoke should update UI mode to experimental successfully`() = runTest {
        // Given
        coEvery { settingsRepository.updateUiMode(any()) } returns Unit

        // When
        val result = useCase(UiMode.EXPERIMENTAL)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { settingsRepository.updateUiMode(UiMode.EXPERIMENTAL) }
    }

    @Test
    fun `invoke should fail when repository throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { settingsRepository.updateUiMode(any()) } throws exception

        // When
        val result = useCase(UiMode.EXPERIMENTAL)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should handle switching from classic to experimental`() = runTest {
        // Given
        coEvery { settingsRepository.updateUiMode(any()) } returns Unit

        // When - switch from classic to experimental
        val result1 = useCase(UiMode.CLASSIC)
        val result2 = useCase(UiMode.EXPERIMENTAL)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        coVerify { settingsRepository.updateUiMode(UiMode.CLASSIC) }
        coVerify { settingsRepository.updateUiMode(UiMode.EXPERIMENTAL) }
    }

    @Test
    fun `invoke should handle switching from experimental to classic`() = runTest {
        // Given
        coEvery { settingsRepository.updateUiMode(any()) } returns Unit

        // When - switch from experimental to classic
        val result1 = useCase(UiMode.EXPERIMENTAL)
        val result2 = useCase(UiMode.CLASSIC)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        coVerify { settingsRepository.updateUiMode(UiMode.EXPERIMENTAL) }
        coVerify { settingsRepository.updateUiMode(UiMode.CLASSIC) }
    }

    @Test
    fun `invoke should handle multiple consecutive updates`() = runTest {
        // Given
        coEvery { settingsRepository.updateUiMode(any()) } returns Unit

        // When - toggle multiple times
        val result1 = useCase(UiMode.EXPERIMENTAL)
        val result2 = useCase(UiMode.CLASSIC)
        val result3 = useCase(UiMode.EXPERIMENTAL)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        assertThat(result3.isSuccess).isTrue()
        coVerify(exactly = 2) { settingsRepository.updateUiMode(UiMode.EXPERIMENTAL) }
        coVerify(exactly = 1) { settingsRepository.updateUiMode(UiMode.CLASSIC) }
    }

    @Test
    fun `invoke should propagate specific exception types`() = runTest {
        // Given
        val exception = IllegalStateException("Settings not initialized")
        coEvery { settingsRepository.updateUiMode(any()) } throws exception

        // When
        val result = useCase(UiMode.CLASSIC)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Settings not initialized")
    }
}
