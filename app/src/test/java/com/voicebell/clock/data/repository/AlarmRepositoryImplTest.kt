package com.voicebell.clock.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.data.local.database.dao.AlarmDao
import com.voicebell.clock.data.local.database.entities.AlarmEntity
import com.voicebell.clock.domain.model.Alarm
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Unit tests for AlarmRepositoryImpl.
 *
 * Tests the repository layer's interaction with the DAO and data mapping.
 */
class AlarmRepositoryImplTest {

    private lateinit var alarmDao: AlarmDao
    private lateinit var repository: AlarmRepositoryImpl

    private val testAlarmEntity = AlarmEntity(
        id = 1,
        hour = 7,
        minute = 0,
        isEnabled = true,
        label = "Test Alarm",
        repeatDays = "1,2,3,4,5",
        vibrate = true,
        gradualVolumeIncrease = true,
        flash = false,
        snoozeEnabled = true,
        snoozeDuration = 5,
        maxSnoozeCount = 3,
        snoozeCount = 0,
        preAlarmCount = 0,
        preAlarmInterval = 7,
        nextTriggerTime = 0
    )

    private val testAlarm = Alarm(
        id = 1,
        time = LocalTime.of(7, 0),
        isEnabled = true,
        label = "Test Alarm",
        repeatDays = setOf(
            com.voicebell.clock.domain.model.DayOfWeek.MONDAY,
            com.voicebell.clock.domain.model.DayOfWeek.TUESDAY,
            com.voicebell.clock.domain.model.DayOfWeek.WEDNESDAY,
            com.voicebell.clock.domain.model.DayOfWeek.THURSDAY,
            com.voicebell.clock.domain.model.DayOfWeek.FRIDAY
        ),
        vibrate = true,
        gradualVolumeIncrease = true,
        flash = false,
        snoozeEnabled = true,
        snoozeDuration = 5,
        maxSnoozeCount = 3
    )

    @Before
    fun setup() {
        alarmDao = mockk()
        repository = AlarmRepositoryImpl(alarmDao)
    }

    // ============================================
    // GET OPERATIONS
    // ============================================

    @Test
    fun `getAllAlarms should return mapped domain models`() = runTest {
        // Given
        every { alarmDao.getAllAlarms() } returns flowOf(listOf(testAlarmEntity))

        // When
        val result = repository.getAllAlarms()

        // Then
        result.test {
            val alarms = awaitItem()
            assertThat(alarms).hasSize(1)
            assertThat(alarms.first().id).isEqualTo(testAlarm.id)
            assertThat(alarms.first().time).isEqualTo(testAlarm.time)
            assertThat(alarms.first().label).isEqualTo(testAlarm.label)
            awaitComplete()
        }
    }

    @Test
    fun `getAllAlarms should return empty list when no alarms`() = runTest {
        // Given
        every { alarmDao.getAllAlarms() } returns flowOf(emptyList())

        // When
        val result = repository.getAllAlarms()

        // Then
        result.test {
            val alarms = awaitItem()
            assertThat(alarms).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getEnabledAlarms should return only enabled alarms`() = runTest {
        // Given
        every { alarmDao.getEnabledAlarms() } returns flowOf(listOf(testAlarmEntity))

        // When
        val result = repository.getEnabledAlarms()

        // Then
        result.test {
            val alarms = awaitItem()
            assertThat(alarms).hasSize(1)
            assertThat(alarms.first().isEnabled).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `getAlarmById should return mapped alarm when found`() = runTest {
        // Given
        coEvery { alarmDao.getAlarmById(1) } returns testAlarmEntity

        // When
        val result = repository.getAlarmById(1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1)
        assertThat(result?.label).isEqualTo("Test Alarm")
    }

    @Test
    fun `getAlarmById should return null when not found`() = runTest {
        // Given
        coEvery { alarmDao.getAlarmById(999) } returns null

        // When
        val result = repository.getAlarmById(999)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getAlarmByIdFlow should emit mapped alarm`() = runTest {
        // Given
        every { alarmDao.getAlarmByIdFlow(1) } returns flowOf(testAlarmEntity)

        // When
        val result = repository.getAlarmByIdFlow(1)

        // Then
        result.test {
            val alarm = awaitItem()
            assertThat(alarm).isNotNull()
            assertThat(alarm?.id).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `getNextScheduledAlarm should return next enabled alarm`() = runTest {
        // Given
        coEvery { alarmDao.getNextScheduledAlarm() } returns testAlarmEntity

        // When
        val result = repository.getNextScheduledAlarm()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.isEnabled).isTrue()
    }

    // ============================================
    // CREATE & UPDATE OPERATIONS
    // ============================================

    @Test
    fun `createAlarm should insert entity and return id`() = runTest {
        // Given
        val newId = 42L
        coEvery { alarmDao.insert(any()) } returns newId

        // When
        val result = repository.createAlarm(testAlarm)

        // Then
        assertThat(result).isEqualTo(newId)
        coVerify { alarmDao.insert(any()) }
    }

    @Test
    fun `updateAlarm should update entity in dao`() = runTest {
        // Given
        coEvery { alarmDao.update(any()) } returns Unit

        // When
        repository.updateAlarm(testAlarm)

        // Then
        coVerify { alarmDao.update(any()) }
    }

    @Test
    fun `deleteAlarm should delete by id`() = runTest {
        // Given
        coEvery { alarmDao.deleteById(1) } returns Unit

        // When
        repository.deleteAlarm(1)

        // Then
        coVerify { alarmDao.deleteById(1) }
    }

    // ============================================
    // TOGGLE & STATE OPERATIONS
    // ============================================

    @Test
    fun `toggleAlarmEnabled should call dao with correct parameters`() = runTest {
        // Given
        coEvery { alarmDao.setEnabled(1, true) } returns Unit

        // When
        repository.toggleAlarmEnabled(1, true)

        // Then
        coVerify { alarmDao.setEnabled(1, true) }
    }

    @Test
    fun `updateNextTriggerTime should call dao`() = runTest {
        // Given
        val triggerTime = 1234567890L
        coEvery { alarmDao.updateNextTriggerTime(1, triggerTime) } returns Unit

        // When
        repository.updateNextTriggerTime(1, triggerTime)

        // Then
        coVerify { alarmDao.updateNextTriggerTime(1, triggerTime) }
    }

    @Test
    fun `updateSnoozeCount should call dao with correct count`() = runTest {
        // Given
        coEvery { alarmDao.updateSnoozeCount(1, 2) } returns Unit

        // When
        repository.updateSnoozeCount(1, 2)

        // Then
        coVerify { alarmDao.updateSnoozeCount(1, 2) }
    }

    @Test
    fun `resetSnoozeCount should call dao`() = runTest {
        // Given
        coEvery { alarmDao.resetSnoozeCount(1) } returns Unit

        // When
        repository.resetSnoozeCount(1)

        // Then
        coVerify { alarmDao.resetSnoozeCount(1) }
    }

    // ============================================
    // COUNT OPERATIONS
    // ============================================

    @Test
    fun `getEnabledAlarmCount should return count from dao`() = runTest {
        // Given
        coEvery { alarmDao.getEnabledAlarmCount() } returns 5

        // When
        val result = repository.getEnabledAlarmCount()

        // Then
        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `getTotalAlarmCount should return total from dao`() = runTest {
        // Given
        coEvery { alarmDao.getTotalAlarmCount() } returns 10

        // When
        val result = repository.getTotalAlarmCount()

        // Then
        assertThat(result).isEqualTo(10)
    }

    // ============================================
    // EDGE CASES
    // ============================================

    @Test
    fun `getAllAlarms should handle multiple emissions`() = runTest {
        // Given
        val entities1 = listOf(testAlarmEntity)
        val entities2 = listOf(testAlarmEntity, testAlarmEntity.copy(id = 2))
        every { alarmDao.getAllAlarms() } returns flowOf(entities1, entities2)

        // When
        val result = repository.getAllAlarms()

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
    fun `repository should properly map repeat days from string to set`() = runTest {
        // Given
        val entityWithDays = testAlarmEntity.copy(repeatDays = "1,3,5,7")
        every { alarmDao.getAllAlarms() } returns flowOf(listOf(entityWithDays))

        // When
        val result = repository.getAllAlarms()

        // Then
        result.test {
            val alarms = awaitItem()
            val alarm = alarms.first()
            assertThat(alarm.repeatDays).containsExactly(
                com.voicebell.clock.domain.model.DayOfWeek.MONDAY,
                com.voicebell.clock.domain.model.DayOfWeek.WEDNESDAY,
                com.voicebell.clock.domain.model.DayOfWeek.FRIDAY,
                com.voicebell.clock.domain.model.DayOfWeek.SUNDAY
            )
            awaitComplete()
        }
    }

    @Test
    fun `repository should handle empty repeat days`() = runTest {
        // Given
        val entityNoDays = testAlarmEntity.copy(repeatDays = "")
        every { alarmDao.getAllAlarms() } returns flowOf(listOf(entityNoDays))

        // When
        val result = repository.getAllAlarms()

        // Then
        result.test {
            val alarms = awaitItem()
            val alarm = alarms.first()
            assertThat(alarm.repeatDays).isEmpty()
            awaitComplete()
        }
    }
}
