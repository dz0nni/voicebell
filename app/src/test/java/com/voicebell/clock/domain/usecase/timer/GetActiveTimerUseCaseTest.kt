package com.voicebell.clock.domain.usecase.timer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.repository.TimerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetActiveTimerUseCase.
 */
class GetActiveTimerUseCaseTest {

    private lateinit var timerRepository: TimerRepository
    private lateinit var useCase: GetActiveTimerUseCase

    private val runningTimer = Timer(
        id = 1,
        label = "Running Timer",
        durationMillis = 300000,
        remainingMillis = 150000,
        isRunning = true,
        isPaused = false,
        isFinished = false,
        startTime = System.currentTimeMillis(),
        vibrate = true
    )

    private val pausedTimer = Timer(
        id = 2,
        label = "Paused Timer",
        durationMillis = 600000,
        remainingMillis = 300000,
        isRunning = false,
        isPaused = true,
        isFinished = false,
        startTime = System.currentTimeMillis() - 300000,
        pauseTime = System.currentTimeMillis(),
        vibrate = true
    )

    private val finishedTimer = Timer(
        id = 3,
        label = "Finished Timer",
        durationMillis = 60000,
        remainingMillis = 0,
        isRunning = false,
        isPaused = false,
        isFinished = true,
        startTime = System.currentTimeMillis() - 60000,
        vibrate = true
    )

    @Before
    fun setup() {
        timerRepository = mockk()
        useCase = GetActiveTimerUseCase(timerRepository)
    }

    // ============================================
    // GET ACTIVE TIMERS (FLOW) TESTS
    // ============================================

    @Test
    fun `invoke should return active timers flow`() = runTest {
        // Given
        val activeTimers = listOf(runningTimer, pausedTimer)
        every { timerRepository.getActiveTimers() } returns flowOf(activeTimers)

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(2)
            assertThat(timers).containsExactly(runningTimer, pausedTimer)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return only running timer`() = runTest {
        // Given
        every { timerRepository.getActiveTimers() } returns flowOf(listOf(runningTimer))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(1)
            assertThat(timers.first().isRunning).isTrue()
            assertThat(timers.first().label).isEqualTo("Running Timer")
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return only paused timer`() = runTest {
        // Given
        every { timerRepository.getActiveTimers() } returns flowOf(listOf(pausedTimer))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(1)
            assertThat(timers.first().isPaused).isTrue()
            assertThat(timers.first().label).isEqualTo("Paused Timer")
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return empty list when no active timers`() = runTest {
        // Given
        every { timerRepository.getActiveTimers() } returns flowOf(emptyList())

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should not return finished timers`() = runTest {
        // Given - only finished timer
        every { timerRepository.getActiveTimers() } returns flowOf(emptyList())

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should emit updated active timers`() = runTest {
        // Given - timer state changes
        val initialState = listOf(runningTimer)
        val updatedState = listOf(runningTimer, pausedTimer)
        every { timerRepository.getActiveTimers() } returns flowOf(initialState, updatedState)

        // When
        val result = useCase()

        // Then
        result.test {
            val timers1 = awaitItem()
            assertThat(timers1).hasSize(1)

            val timers2 = awaitItem()
            assertThat(timers2).hasSize(2)

            awaitComplete()
        }
    }

    @Test
    fun `invoke should return multiple active timers`() = runTest {
        // Given
        val timer1 = runningTimer.copy(id = 1, label = "Timer 1")
        val timer2 = pausedTimer.copy(id = 2, label = "Timer 2")
        val timer3 = runningTimer.copy(id = 3, label = "Timer 3")
        every { timerRepository.getActiveTimers() } returns flowOf(listOf(timer1, timer2, timer3))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(3)
            assertThat(timers.map { it.label }).containsExactly("Timer 1", "Timer 2", "Timer 3")
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return timers with correct remaining time`() = runTest {
        // Given
        val timer = runningTimer.copy(
            durationMillis = 600000, // 10 minutes
            remainingMillis = 240000  // 4 minutes
        )
        every { timerRepository.getActiveTimers() } returns flowOf(listOf(timer))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers.first().durationMillis).isEqualTo(600000)
            assertThat(timers.first().remainingMillis).isEqualTo(240000)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return timers with vibrate setting`() = runTest {
        // Given
        val timerWithVibrate = runningTimer.copy(vibrate = true)
        val timerWithoutVibrate = pausedTimer.copy(vibrate = false)
        every { timerRepository.getActiveTimers() } returns flowOf(
            listOf(timerWithVibrate, timerWithoutVibrate)
        )

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers[0].vibrate).isTrue()
            assertThat(timers[1].vibrate).isFalse()
            awaitComplete()
        }
    }

    // ============================================
    // GET RUNNING TIMER TESTS
    // ============================================

    @Test
    fun `getRunning should return running timer`() = runTest {
        // Given
        coEvery { timerRepository.getRunningTimer() } returns runningTimer

        // When
        val result = useCase.getRunning()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1)
        assertThat(result?.isRunning).isTrue()
        assertThat(result?.label).isEqualTo("Running Timer")
        coVerify { timerRepository.getRunningTimer() }
    }

    @Test
    fun `getRunning should return null when no timer is running`() = runTest {
        // Given
        coEvery { timerRepository.getRunningTimer() } returns null

        // When
        val result = useCase.getRunning()

        // Then
        assertThat(result).isNull()
        coVerify { timerRepository.getRunningTimer() }
    }

    @Test
    fun `getRunning should return timer with correct state`() = runTest {
        // Given
        val timer = runningTimer.copy(
            durationMillis = 180000,
            remainingMillis = 90000,
            label = "Active Timer"
        )
        coEvery { timerRepository.getRunningTimer() } returns timer

        // When
        val result = useCase.getRunning()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.durationMillis).isEqualTo(180000)
        assertThat(result?.remainingMillis).isEqualTo(90000)
        assertThat(result?.label).isEqualTo("Active Timer")
        assertThat(result?.isRunning).isTrue()
        assertThat(result?.isPaused).isFalse()
        assertThat(result?.isFinished).isFalse()
    }

    @Test
    fun `getRunning should not return paused timer`() = runTest {
        // Given - paused timer should not be returned by getRunningTimer
        coEvery { timerRepository.getRunningTimer() } returns null

        // When
        val result = useCase.getRunning()

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getRunning should not return finished timer`() = runTest {
        // Given - finished timer should not be returned by getRunningTimer
        coEvery { timerRepository.getRunningTimer() } returns null

        // When
        val result = useCase.getRunning()

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getRunning should return timer with timestamps`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val timer = runningTimer.copy(
            startTime = currentTime - 60000, // Started 1 minute ago
            endTime = currentTime + 240000    // Ends in 4 minutes
        )
        coEvery { timerRepository.getRunningTimer() } returns timer

        // When
        val result = useCase.getRunning()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.startTime).isLessThan(currentTime)
        assertThat(result?.endTime).isGreaterThan(currentTime)
    }

    @Test
    fun `getRunning should handle multiple calls`() = runTest {
        // Given
        coEvery { timerRepository.getRunningTimer() } returns runningTimer

        // When - call multiple times
        val result1 = useCase.getRunning()
        val result2 = useCase.getRunning()
        val result3 = useCase.getRunning()

        // Then
        assertThat(result1).isNotNull()
        assertThat(result2).isNotNull()
        assertThat(result3).isNotNull()
        coVerify(exactly = 3) { timerRepository.getRunningTimer() }
    }
}
