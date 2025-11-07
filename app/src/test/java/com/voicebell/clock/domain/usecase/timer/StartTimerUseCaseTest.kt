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
 * Unit tests for StartTimerUseCase.
 */
class StartTimerUseCaseTest {

    private lateinit var timerRepository: TimerRepository
    private lateinit var useCase: StartTimerUseCase

    private val testTimer = Timer(
        id = 1,
        label = "Test Timer",
        durationMillis = 300000, // 5 minutes
        remainingMillis = 300000,
        isRunning = true,
        isPaused = false,
        isFinished = false,
        startTime = System.currentTimeMillis(),
        vibrate = true
    )

    @Before
    fun setup() {
        timerRepository = mockk()
        useCase = StartTimerUseCase(timerRepository)
    }

    // ============================================
    // START NEW TIMER TESTS
    // ============================================

    @Test
    fun `invoke should create and start new timer successfully`() = runTest {
        // Given
        val durationMillis = 300000L // 5 minutes
        val label = "Test Timer"
        coEvery { timerRepository.getRunningTimer() } returns null
        coEvery { timerRepository.createTimer(any()) } returns 1L

        // When
        val result = useCase(durationMillis, label, vibrate = true)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1L)
        coVerify { timerRepository.getRunningTimer() }
        coVerify { timerRepository.createTimer(any()) }
    }

    @Test
    fun `invoke should create timer with default values when not specified`() = runTest {
        // Given
        val durationMillis = 60000L // 1 minute
        coEvery { timerRepository.getRunningTimer() } returns null
        coEvery { timerRepository.createTimer(any()) } returns 2L

        // When
        val result = useCase(durationMillis)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(2L)
        coVerify {
            timerRepository.createTimer(match { timer ->
                timer.label == "" &&
                timer.vibrate == true &&
                timer.durationMillis == durationMillis &&
                timer.remainingMillis == durationMillis &&
                timer.isRunning &&
                !timer.isPaused &&
                !timer.isFinished
            })
        }
    }

    @Test
    fun `invoke should fail when duration is zero`() = runTest {
        // When
        val result = useCase(durationMillis = 0)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Duration must be positive")
    }

    @Test
    fun `invoke should fail when duration is negative`() = runTest {
        // When
        val result = useCase(durationMillis = -1000)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Duration must be positive")
    }

    @Test
    fun `invoke should fail when another timer is already running`() = runTest {
        // Given
        coEvery { timerRepository.getRunningTimer() } returns testTimer

        // When
        val result = useCase(durationMillis = 60000)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("already running")
        coVerify(exactly = 0) { timerRepository.createTimer(any()) }
    }

    @Test
    fun `invoke should fail when repository throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { timerRepository.getRunningTimer() } returns null
        coEvery { timerRepository.createTimer(any()) } throws exception

        // When
        val result = useCase(durationMillis = 60000)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should create timer with correct timestamps`() = runTest {
        // Given
        val durationMillis = 120000L // 2 minutes
        val beforeStartTime = System.currentTimeMillis()
        coEvery { timerRepository.getRunningTimer() } returns null
        coEvery { timerRepository.createTimer(any()) } returns 3L

        // When
        val result = useCase(durationMillis)
        val afterStartTime = System.currentTimeMillis()

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.createTimer(match { timer ->
                timer.startTime >= beforeStartTime &&
                timer.startTime <= afterStartTime &&
                timer.endTime >= beforeStartTime + durationMillis &&
                timer.endTime <= afterStartTime + durationMillis
            })
        }
    }

    // ============================================
    // RESUME TIMER TESTS
    // ============================================

    @Test
    fun `resume should resume paused timer successfully`() = runTest {
        // Given
        val pausedTimer = testTimer.copy(
            isRunning = false,
            isPaused = true
        )
        coEvery { timerRepository.getTimerById(1) } returns pausedTimer
        coEvery { timerRepository.updateTimer(any()) } returns Unit

        // When
        val result = useCase.resume(1)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.getTimerById(1) }
        coVerify {
            timerRepository.updateTimer(match { timer ->
                timer.isRunning &&
                !timer.isPaused
            })
        }
    }

    @Test
    fun `resume should fail when timer not found`() = runTest {
        // Given
        coEvery { timerRepository.getTimerById(999) } returns null

        // When
        val result = useCase.resume(999)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Timer not found")
    }

    @Test
    fun `resume should fail when timer is not paused`() = runTest {
        // Given - timer is running, not paused
        val runningTimer = testTimer.copy(
            isRunning = true,
            isPaused = false
        )
        coEvery { timerRepository.getTimerById(1) } returns runningTimer

        // When
        val result = useCase.resume(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("not paused")
        coVerify(exactly = 0) { timerRepository.updateTimer(any()) }
    }

    @Test
    fun `resume should fail when timer is finished`() = runTest {
        // Given - timer is finished
        val finishedTimer = testTimer.copy(
            isRunning = false,
            isPaused = false,
            isFinished = true
        )
        coEvery { timerRepository.getTimerById(1) } returns finishedTimer

        // When
        val result = useCase.resume(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `resume should update start time when resuming`() = runTest {
        // Given
        val pausedTimer = testTimer.copy(
            isRunning = false,
            isPaused = true,
            startTime = System.currentTimeMillis() - 10000 // Old start time
        )
        val beforeResumeTime = System.currentTimeMillis()
        coEvery { timerRepository.getTimerById(1) } returns pausedTimer
        coEvery { timerRepository.updateTimer(any()) } returns Unit

        // When
        val result = useCase.resume(1)
        val afterResumeTime = System.currentTimeMillis()

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.updateTimer(match { timer ->
                timer.startTime >= beforeResumeTime &&
                timer.startTime <= afterResumeTime
            })
        }
    }

    @Test
    fun `resume should fail when repository throws exception`() = runTest {
        // Given
        val pausedTimer = testTimer.copy(isPaused = true)
        val exception = RuntimeException("Database error")
        coEvery { timerRepository.getTimerById(1) } returns pausedTimer
        coEvery { timerRepository.updateTimer(any()) } throws exception

        // When
        val result = useCase.resume(1)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    // ============================================
    // EDGE CASES
    // ============================================

    @Test
    fun `invoke should handle very short duration`() = runTest {
        // Given - 1 second timer
        val durationMillis = 1000L
        coEvery { timerRepository.getRunningTimer() } returns null
        coEvery { timerRepository.createTimer(any()) } returns 5L

        // When
        val result = useCase(durationMillis)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.createTimer(match { timer ->
                timer.durationMillis == 1000L
            })
        }
    }

    @Test
    fun `invoke should handle very long duration`() = runTest {
        // Given - 24 hours timer
        val durationMillis = 86400000L
        coEvery { timerRepository.getRunningTimer() } returns null
        coEvery { timerRepository.createTimer(any()) } returns 6L

        // When
        val result = useCase(durationMillis)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.createTimer(match { timer ->
                timer.durationMillis == 86400000L
            })
        }
    }

    @Test
    fun `invoke should preserve vibrate setting`() = runTest {
        // Given
        coEvery { timerRepository.getRunningTimer() } returns null
        coEvery { timerRepository.createTimer(any()) } returns 7L

        // When
        val result = useCase(durationMillis = 60000, vibrate = false)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            timerRepository.createTimer(match { timer ->
                !timer.vibrate
            })
        }
    }
}
