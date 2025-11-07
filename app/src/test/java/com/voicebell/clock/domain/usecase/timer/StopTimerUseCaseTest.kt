package com.voicebell.clock.domain.usecase.timer

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.repository.TimerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for StopTimerUseCase.
 */
class StopTimerUseCaseTest {

    private lateinit var timerRepository: TimerRepository
    private lateinit var useCase: StopTimerUseCase

    private val testTimer = Timer(
        id = 1,
        label = "Test Timer",
        durationMillis = 300000, // 5 minutes
        remainingMillis = 150000, // 2.5 minutes left
        isRunning = true,
        isPaused = false,
        isFinished = false,
        startTime = System.currentTimeMillis(),
        vibrate = true
    )

    @Before
    fun setup() {
        timerRepository = mockk()
        useCase = StopTimerUseCase(timerRepository)
    }

    @Test
    fun `invoke should stop running timer successfully`() = runTest {
        // Given
        coEvery { timerRepository.getTimerById(1) } returns testTimer
        coEvery { timerRepository.markAsFinished(1) } returns Unit

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.getTimerById(1) }
        coVerify { timerRepository.markAsFinished(1) }
    }

    @Test
    fun `invoke should stop paused timer successfully`() = runTest {
        // Given
        val pausedTimer = testTimer.copy(
            isRunning = false,
            isPaused = true
        )
        coEvery { timerRepository.getTimerById(1) } returns pausedTimer
        coEvery { timerRepository.markAsFinished(1) } returns Unit

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.markAsFinished(1) }
    }

    @Test
    fun `invoke should fail when timer not found`() = runTest {
        // Given
        coEvery { timerRepository.getTimerById(999) } returns null

        // When
        val result = useCase(999)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Timer not found")
        coVerify(exactly = 0) { timerRepository.markAsFinished(any()) }
    }

    @Test
    fun `invoke should fail when repository throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { timerRepository.getTimerById(1) } returns testTimer
        coEvery { timerRepository.markAsFinished(1) } throws exception

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should stop timer even if already finished`() = runTest {
        // Given - timer already finished
        val finishedTimer = testTimer.copy(
            isRunning = false,
            isPaused = false,
            isFinished = true,
            remainingMillis = 0
        )
        coEvery { timerRepository.getTimerById(1) } returns finishedTimer
        coEvery { timerRepository.markAsFinished(1) } returns Unit

        // When
        val result = useCase(1)

        // Then - should still succeed (idempotent operation)
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.markAsFinished(1) }
    }

    @Test
    fun `invoke should stop timer with any remaining time`() = runTest {
        // Given - timer with 10 seconds left
        val almostDoneTimer = testTimer.copy(
            remainingMillis = 10000
        )
        coEvery { timerRepository.getTimerById(1) } returns almostDoneTimer
        coEvery { timerRepository.markAsFinished(1) } returns Unit

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.markAsFinished(1) }
    }

    @Test
    fun `invoke should handle multiple consecutive stops`() = runTest {
        // Given
        coEvery { timerRepository.getTimerById(1) } returns testTimer
        coEvery { timerRepository.markAsFinished(1) } returns Unit

        // When - stop twice
        val result1 = useCase(1)
        val result2 = useCase(1)

        // Then - both should succeed
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        coVerify(exactly = 2) { timerRepository.markAsFinished(1) }
    }
}
