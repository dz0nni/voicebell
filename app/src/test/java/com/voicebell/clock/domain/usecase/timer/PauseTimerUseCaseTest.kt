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
 * Unit tests for PauseTimerUseCase.
 */
class PauseTimerUseCaseTest {

    private lateinit var timerRepository: TimerRepository
    private lateinit var useCase: PauseTimerUseCase

    private val testTimer = Timer(
        id = 1,
        label = "Test Timer",
        durationMillis = 300000, // 5 minutes
        remainingMillis = 150000, // 2.5 minutes left
        isRunning = true,
        isPaused = false,
        isFinished = false,
        startTime = System.currentTimeMillis() - 150000, // Started 2.5 min ago
        vibrate = true
    )

    @Before
    fun setup() {
        timerRepository = mockk()
        useCase = PauseTimerUseCase(timerRepository)
    }

    @Test
    fun `invoke should pause running timer successfully`() = runTest {
        // Given
        coEvery { timerRepository.getTimerById(1) } returns testTimer
        coEvery { timerRepository.updateTimer(any()) } returns Unit

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.getTimerById(1) }
        coVerify {
            timerRepository.updateTimer(match { timer ->
                !timer.isRunning &&
                timer.isPaused &&
                timer.pauseTime > 0
            })
        }
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
        coVerify(exactly = 0) { timerRepository.updateTimer(any()) }
    }

    @Test
    fun `invoke should fail when timer is not running`() = runTest {
        // Given - timer is stopped
        val stoppedTimer = testTimer.copy(
            isRunning = false,
            isPaused = false
        )
        coEvery { timerRepository.getTimerById(1) } returns stoppedTimer

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("not running")
        coVerify(exactly = 0) { timerRepository.updateTimer(any()) }
    }

    @Test
    fun `invoke should fail when timer is already paused`() = runTest {
        // Given - timer already paused
        val pausedTimer = testTimer.copy(
            isRunning = false,
            isPaused = true
        )
        coEvery { timerRepository.getTimerById(1) } returns pausedTimer

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("not running")
        coVerify(exactly = 0) { timerRepository.updateTimer(any()) }
    }

    @Test
    fun `invoke should fail when timer is finished`() = runTest {
        // Given
        val finishedTimer = testTimer.copy(
            isRunning = false,
            isPaused = false,
            isFinished = true,
            remainingMillis = 0
        )
        coEvery { timerRepository.getTimerById(1) } returns finishedTimer

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `invoke should calculate remaining time correctly`() = runTest {
        // Given - timer that just started
        val justStartedTimer = testTimer.copy(
            durationMillis = 60000, // 1 minute
            remainingMillis = 60000,
            startTime = System.currentTimeMillis()
        )
        coEvery { timerRepository.getTimerById(1) } returns justStartedTimer
        coEvery { timerRepository.updateTimer(any()) } returns Unit

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.updateTimer(match { timer ->
                // Remaining time should be close to original (allowing for small execution time)
                timer.remainingMillis > 55000 && // At least 55 seconds
                timer.remainingMillis <= 60000 // Max 60 seconds
            })
        }
    }

    @Test
    fun `invoke should set pause time to current time`() = runTest {
        // Given
        val beforePauseTime = System.currentTimeMillis()
        coEvery { timerRepository.getTimerById(1) } returns testTimer
        coEvery { timerRepository.updateTimer(any()) } returns Unit

        // When
        val result = useCase(1)
        val afterPauseTime = System.currentTimeMillis()

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.updateTimer(match { timer ->
                timer.pauseTime >= beforePauseTime &&
                timer.pauseTime <= afterPauseTime
            })
        }
    }

    @Test
    fun `invoke should fail when repository throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { timerRepository.getTimerById(1) } returns testTimer
        coEvery { timerRepository.updateTimer(any()) } throws exception

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should preserve timer label and settings`() = runTest {
        // Given
        val timerWithLabel = testTimer.copy(
            label = "Important Timer",
            vibrate = false
        )
        coEvery { timerRepository.getTimerById(1) } returns timerWithLabel
        coEvery { timerRepository.updateTimer(any()) } returns Unit

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.updateTimer(match { timer ->
                timer.label == "Important Timer" &&
                !timer.vibrate
            })
        }
    }

    @Test
    fun `invoke should handle timer with very little time remaining`() = runTest {
        // Given - timer with 1 second left
        val almostDoneTimer = testTimer.copy(
            durationMillis = 1000,
            remainingMillis = 1000,
            startTime = System.currentTimeMillis()
        )
        coEvery { timerRepository.getTimerById(1) } returns almostDoneTimer
        coEvery { timerRepository.updateTimer(any()) } returns Unit

        // When
        val result = useCase(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.updateTimer(match { timer ->
                timer.isPaused &&
                timer.remainingMillis >= 0
            })
        }
    }
}
