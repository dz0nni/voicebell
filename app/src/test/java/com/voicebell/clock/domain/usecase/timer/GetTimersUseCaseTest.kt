package com.voicebell.clock.domain.usecase.timer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.repository.TimerRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetTimersUseCase.
 */
class GetTimersUseCaseTest {

    private lateinit var timerRepository: TimerRepository
    private lateinit var useCase: GetTimersUseCase

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
        useCase = GetTimersUseCase(timerRepository)
    }

    @Test
    fun `invoke should return all timers flow`() = runTest {
        // Given
        val allTimers = listOf(runningTimer, pausedTimer, finishedTimer)
        every { timerRepository.getAllTimers() } returns flowOf(allTimers)

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(3)
            assertThat(timers).containsExactly(runningTimer, pausedTimer, finishedTimer)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return empty list when no timers exist`() = runTest {
        // Given
        every { timerRepository.getAllTimers() } returns flowOf(emptyList())

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
    fun `invoke should return only running timer`() = runTest {
        // Given
        every { timerRepository.getAllTimers() } returns flowOf(listOf(runningTimer))

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
        every { timerRepository.getAllTimers() } returns flowOf(listOf(pausedTimer))

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
    fun `invoke should return only finished timer`() = runTest {
        // Given
        every { timerRepository.getAllTimers() } returns flowOf(listOf(finishedTimer))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(1)
            assertThat(timers.first().isFinished).isTrue()
            assertThat(timers.first().label).isEqualTo("Finished Timer")
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return multiple timers of different states`() = runTest {
        // Given
        val timer1 = runningTimer.copy(id = 1, label = "Timer 1")
        val timer2 = pausedTimer.copy(id = 2, label = "Timer 2")
        val timer3 = finishedTimer.copy(id = 3, label = "Timer 3")
        val timer4 = runningTimer.copy(id = 4, label = "Timer 4")
        every { timerRepository.getAllTimers() } returns flowOf(
            listOf(timer1, timer2, timer3, timer4)
        )

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(4)
            assertThat(timers.map { it.label }).containsExactly(
                "Timer 1", "Timer 2", "Timer 3", "Timer 4"
            )
            awaitComplete()
        }
    }

    @Test
    fun `invoke should emit updated timer list`() = runTest {
        // Given - timer list changes
        val initialTimers = listOf(runningTimer)
        val updatedTimers = listOf(runningTimer, pausedTimer)
        every { timerRepository.getAllTimers() } returns flowOf(initialTimers, updatedTimers)

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
    fun `invoke should return timers with correct properties`() = runTest {
        // Given
        val timer = Timer(
            id = 5,
            label = "Test Timer",
            durationMillis = 900000, // 15 minutes
            remainingMillis = 450000, // 7.5 minutes
            isRunning = true,
            isPaused = false,
            isFinished = false,
            startTime = System.currentTimeMillis() - 450000,
            endTime = System.currentTimeMillis() + 450000,
            vibrate = false
        )
        every { timerRepository.getAllTimers() } returns flowOf(listOf(timer))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(1)
            val t = timers.first()
            assertThat(t.id).isEqualTo(5)
            assertThat(t.label).isEqualTo("Test Timer")
            assertThat(t.durationMillis).isEqualTo(900000)
            assertThat(t.remainingMillis).isEqualTo(450000)
            assertThat(t.isRunning).isTrue()
            assertThat(t.isPaused).isFalse()
            assertThat(t.isFinished).isFalse()
            assertThat(t.vibrate).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return timers with empty labels`() = runTest {
        // Given
        val timerWithoutLabel = runningTimer.copy(label = "")
        every { timerRepository.getAllTimers() } returns flowOf(listOf(timerWithoutLabel))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers.first().label).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return timers with custom labels`() = runTest {
        // Given
        val timer1 = runningTimer.copy(label = "Workout Timer ⏱")
        val timer2 = pausedTimer.copy(label = "Cooking Timer 🍳")
        every { timerRepository.getAllTimers() } returns flowOf(listOf(timer1, timer2))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers[0].label).isEqualTo("Workout Timer ⏱")
            assertThat(timers[1].label).isEqualTo("Cooking Timer 🍳")
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return timers with various durations`() = runTest {
        // Given
        val shortTimer = runningTimer.copy(durationMillis = 30000) // 30 seconds
        val mediumTimer = pausedTimer.copy(durationMillis = 600000) // 10 minutes
        val longTimer = finishedTimer.copy(durationMillis = 3600000) // 1 hour
        every { timerRepository.getAllTimers() } returns flowOf(
            listOf(shortTimer, mediumTimer, longTimer)
        )

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers[0].durationMillis).isEqualTo(30000)
            assertThat(timers[1].durationMillis).isEqualTo(600000)
            assertThat(timers[2].durationMillis).isEqualTo(3600000)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return timers sorted by repository order`() = runTest {
        // Given - repository returns timers in specific order
        val timer1 = runningTimer.copy(id = 10)
        val timer2 = pausedTimer.copy(id = 5)
        val timer3 = finishedTimer.copy(id = 1)
        every { timerRepository.getAllTimers() } returns flowOf(listOf(timer1, timer2, timer3))

        // When
        val result = useCase()

        // Then - should maintain repository order
        result.test {
            val timers = awaitItem()
            assertThat(timers[0].id).isEqualTo(10)
            assertThat(timers[1].id).isEqualTo(5)
            assertThat(timers[2].id).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should handle timer state transitions`() = runTest {
        // Given - timer changes from running to paused to finished
        val runningState = listOf(runningTimer)
        val pausedState = listOf(runningTimer.copy(isRunning = false, isPaused = true))
        val finishedState = listOf(
            runningTimer.copy(
                isRunning = false,
                isPaused = false,
                isFinished = true,
                remainingMillis = 0
            )
        )
        every { timerRepository.getAllTimers() } returns flowOf(
            runningState,
            pausedState,
            finishedState
        )

        // When
        val result = useCase()

        // Then
        result.test {
            val state1 = awaitItem()
            assertThat(state1.first().isRunning).isTrue()

            val state2 = awaitItem()
            assertThat(state2.first().isPaused).isTrue()

            val state3 = awaitItem()
            assertThat(state3.first().isFinished).isTrue()

            awaitComplete()
        }
    }

    @Test
    fun `invoke should return timers with vibrate enabled and disabled`() = runTest {
        // Given
        val vibrateOn = runningTimer.copy(vibrate = true)
        val vibrateOff = pausedTimer.copy(vibrate = false)
        every { timerRepository.getAllTimers() } returns flowOf(listOf(vibrateOn, vibrateOff))

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

    @Test
    fun `invoke should return timers with correct timestamps`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val timer = runningTimer.copy(
            startTime = currentTime - 120000, // Started 2 minutes ago
            endTime = currentTime + 180000,   // Ends in 3 minutes
            pauseTime = 0
        )
        every { timerRepository.getAllTimers() } returns flowOf(listOf(timer))

        // When
        val result = useCase()

        // Then
        result.test {
            val timers = awaitItem()
            val t = timers.first()
            assertThat(t.startTime).isLessThan(currentTime)
            assertThat(t.endTime).isGreaterThan(currentTime)
            awaitComplete()
        }
    }
}
