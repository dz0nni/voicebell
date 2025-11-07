package com.voicebell.clock.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.data.local.database.dao.TimerDao
import com.voicebell.clock.data.local.database.entities.TimerEntity
import com.voicebell.clock.domain.model.Timer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TimerRepositoryImpl.
 *
 * Tests the repository layer's interaction with the DAO and data mapping.
 */
class TimerRepositoryImplTest {

    private lateinit var timerDao: TimerDao
    private lateinit var repository: TimerRepositoryImpl

    private val testTimerEntity = TimerEntity(
        id = 1,
        label = "Test Timer",
        durationMillis = 300000, // 5 minutes
        remainingMillis = 300000,
        isRunning = true,
        isPaused = false,
        isFinished = false,
        startTime = System.currentTimeMillis(),
        pauseTime = 0,
        endTime = 0,
        vibrate = true,
        ringtone = "default",
        createdAt = System.currentTimeMillis()
    )

    private val testTimer = Timer(
        id = 1,
        label = "Test Timer",
        durationMillis = 300000,
        remainingMillis = 300000,
        isRunning = true,
        isPaused = false,
        isFinished = false,
        startTime = System.currentTimeMillis(),
        pauseTime = 0,
        endTime = 0,
        vibrate = true,
        ringtone = "default"
    )

    @Before
    fun setup() {
        timerDao = mockk()
        repository = TimerRepositoryImpl(timerDao)
    }

    // ============================================
    // GET OPERATIONS
    // ============================================

    @Test
    fun `getAllTimers should return mapped domain models`() = runTest {
        // Given
        every { timerDao.getAllTimers() } returns flowOf(listOf(testTimerEntity))

        // When
        val result = repository.getAllTimers()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(1)
            assertThat(timers.first().id).isEqualTo(testTimer.id)
            assertThat(timers.first().label).isEqualTo(testTimer.label)
            assertThat(timers.first().durationMillis).isEqualTo(testTimer.durationMillis)
            awaitComplete()
        }
    }

    @Test
    fun `getAllTimers should return empty list when no timers`() = runTest {
        // Given
        every { timerDao.getAllTimers() } returns flowOf(emptyList())

        // When
        val result = repository.getAllTimers()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getActiveTimers should return only active timers`() = runTest {
        // Given
        every { timerDao.getActiveTimers() } returns flowOf(listOf(testTimerEntity))

        // When
        val result = repository.getActiveTimers()

        // Then
        result.test {
            val timers = awaitItem()
            assertThat(timers).hasSize(1)
            assertThat(timers.first().isRunning).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `getTimerById should return mapped timer when found`() = runTest {
        // Given
        coEvery { timerDao.getTimerById(1) } returns testTimerEntity

        // When
        val result = repository.getTimerById(1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1)
        assertThat(result?.label).isEqualTo("Test Timer")
    }

    @Test
    fun `getTimerById should return null when not found`() = runTest {
        // Given
        coEvery { timerDao.getTimerById(999) } returns null

        // When
        val result = repository.getTimerById(999)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getTimerByIdFlow should emit mapped timer`() = runTest {
        // Given
        every { timerDao.getTimerByIdFlow(1) } returns flowOf(testTimerEntity)

        // When
        val result = repository.getTimerByIdFlow(1)

        // Then
        result.test {
            val timer = awaitItem()
            assertThat(timer).isNotNull()
            assertThat(timer?.id).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `getRunningTimer should return running timer`() = runTest {
        // Given
        coEvery { timerDao.getRunningTimer() } returns testTimerEntity

        // When
        val result = repository.getRunningTimer()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.isRunning).isTrue()
        assertThat(result?.isPaused).isFalse()
    }

    @Test
    fun `getRunningTimer should return null when no timer running`() = runTest {
        // Given
        coEvery { timerDao.getRunningTimer() } returns null

        // When
        val result = repository.getRunningTimer()

        // Then
        assertThat(result).isNull()
    }

    // ============================================
    // CREATE & UPDATE OPERATIONS
    // ============================================

    @Test
    fun `createTimer should insert entity and return id`() = runTest {
        // Given
        val newId = 42L
        coEvery { timerDao.insert(any()) } returns newId

        // When
        val result = repository.createTimer(testTimer)

        // Then
        assertThat(result).isEqualTo(newId)
        coVerify { timerDao.insert(any()) }
    }

    @Test
    fun `updateTimer should update entity in dao`() = runTest {
        // Given
        coEvery { timerDao.update(any()) } returns Unit

        // When
        repository.updateTimer(testTimer)

        // Then
        coVerify { timerDao.update(any()) }
    }

    @Test
    fun `deleteTimer should delete by id`() = runTest {
        // Given
        coEvery { timerDao.deleteById(1) } returns Unit

        // When
        repository.deleteTimer(1)

        // Then
        coVerify { timerDao.deleteById(1) }
    }

    @Test
    fun `deleteFinishedTimers should call dao`() = runTest {
        // Given
        coEvery { timerDao.deleteFinishedTimers() } returns Unit

        // When
        repository.deleteFinishedTimers()

        // Then
        coVerify { timerDao.deleteFinishedTimers() }
    }

    // ============================================
    // STATE OPERATIONS
    // ============================================

    @Test
    fun `updateTimerState should call dao with correct parameters`() = runTest {
        // Given
        coEvery { timerDao.updateState(1, true, false) } returns Unit

        // When
        repository.updateTimerState(1, isRunning = true, isPaused = false)

        // Then
        coVerify { timerDao.updateState(1, true, false) }
    }

    @Test
    fun `updateRemainingTime should call dao`() = runTest {
        // Given
        val remainingMillis = 250000L
        coEvery { timerDao.updateRemainingTime(1, remainingMillis) } returns Unit

        // When
        repository.updateRemainingTime(1, remainingMillis)

        // Then
        coVerify { timerDao.updateRemainingTime(1, remainingMillis) }
    }

    @Test
    fun `markAsFinished should call dao`() = runTest {
        // Given
        coEvery { timerDao.markAsFinished(1) } returns Unit

        // When
        repository.markAsFinished(1)

        // Then
        coVerify { timerDao.markAsFinished(1) }
    }

    // ============================================
    // COUNT OPERATIONS
    // ============================================

    @Test
    fun `getActiveTimerCount should return count from dao`() = runTest {
        // Given
        coEvery { timerDao.getActiveTimerCount() } returns 3

        // When
        val result = repository.getActiveTimerCount()

        // Then
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `getActiveTimerCount should return zero when no timers`() = runTest {
        // Given
        coEvery { timerDao.getActiveTimerCount() } returns 0

        // When
        val result = repository.getActiveTimerCount()

        // Then
        assertThat(result).isEqualTo(0)
    }

    // ============================================
    // EDGE CASES
    // ============================================

    @Test
    fun `getAllTimers should handle multiple emissions`() = runTest {
        // Given
        val timers1 = listOf(testTimerEntity)
        val timers2 = listOf(testTimerEntity, testTimerEntity.copy(id = 2))
        every { timerDao.getAllTimers() } returns flowOf(timers1, timers2)

        // When
        val result = repository.getAllTimers()

        // Then
        result.test {
            val first = awaitItem()
            assertThat(first).hasSize(1)

            val second = awaitItem()
            assertThat(second).hasSize(2)

            awaitComplete()
        }
    }

    @Test
    fun `repository should map timer with zero remaining time`() = runTest {
        // Given
        val finishedEntity = testTimerEntity.copy(
            remainingMillis = 0,
            isRunning = false,
            isFinished = true
        )
        every { timerDao.getAllTimers() } returns flowOf(listOf(finishedEntity))

        // When
        val result = repository.getAllTimers()

        // Then
        result.test {
            val timers = awaitItem()
            val timer = timers.first()
            assertThat(timer.remainingMillis).isEqualTo(0)
            assertThat(timer.isFinished).isTrue()
            assertThat(timer.isRunning).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `repository should map paused timer correctly`() = runTest {
        // Given
        val pausedEntity = testTimerEntity.copy(
            isRunning = true,
            isPaused = true
        )
        coEvery { timerDao.getRunningTimer() } returns pausedEntity

        // When
        val result = repository.getRunningTimer()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.isRunning).isTrue()
        assertThat(result?.isPaused).isTrue()
    }
}
