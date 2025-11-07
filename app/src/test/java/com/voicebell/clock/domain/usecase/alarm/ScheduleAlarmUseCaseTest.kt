package com.voicebell.clock.domain.usecase.alarm

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.AlarmTone
import com.voicebell.clock.domain.model.DayOfWeek
import com.voicebell.clock.domain.repository.AlarmRepository
import com.voicebell.clock.util.AlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Unit tests for ScheduleAlarmUseCase.
 */
class ScheduleAlarmUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var useCase: ScheduleAlarmUseCase

    private val testAlarm = Alarm(
        id = 1,
        time = LocalTime.of(7, 0),
        isEnabled = true,
        label = "Morning Alarm",
        alarmTone = AlarmTone.DEFAULT,
        repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
        vibrate = true,
        gradualVolumeIncrease = true,
        snoozeEnabled = true,
        snoozeDuration = 5,
        maxSnoozeCount = 3
    )

    @Before
    fun setup() {
        alarmRepository = mockk()
        alarmScheduler = mockk()
        useCase = ScheduleAlarmUseCase(alarmRepository, alarmScheduler)
    }

    // ============================================
    // SCHEDULE ALARM TESTS
    // ============================================

    @Test
    fun `invoke should schedule alarm successfully`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { alarmScheduler.canScheduleExactAlarms() }
        coVerify { alarmRepository.updateAlarm(testAlarm) }
        coVerify { alarmRepository.updateNextTriggerTime(1, any()) }
        verify { alarmScheduler.scheduleAlarm(testAlarm) }
    }

    @Test
    fun `invoke should fail when cannot schedule exact alarms`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns false

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(SecurityException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Cannot schedule exact alarms")
        verify(exactly = 0) { alarmScheduler.scheduleAlarm(any()) }
    }

    @Test
    fun `invoke should schedule disabled alarm successfully`() = runTest {
        // Given - disabled alarm should still be scheduled (AlarmScheduler handles the disabled state)
        val disabledAlarm = testAlarm.copy(isEnabled = false)
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(disabledAlarm)

        // Then - should succeed, AlarmScheduler will handle the disabled state internally
        assertThat(result.isSuccess).isTrue()
        verify { alarmScheduler.scheduleAlarm(disabledAlarm) }
    }

    @Test
    fun `invoke should update database before scheduling`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            alarmRepository.updateAlarm(testAlarm)
            alarmRepository.updateNextTriggerTime(1, any())
            alarmScheduler.scheduleAlarm(testAlarm)
        }
    }

    @Test
    fun `invoke should calculate next trigger time`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateNextTriggerTime(1, match { triggerTime ->
                triggerTime > System.currentTimeMillis()
            })
        }
    }

    @Test
    fun `invoke should schedule alarm with repeat days`() = runTest {
        // Given
        val repeatingAlarm = testAlarm.copy(
            repeatDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY
            )
        )
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(repeatingAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify {
            alarmScheduler.scheduleAlarm(match { alarm ->
                alarm.repeatDays.size == 3
            })
        }
    }

    @Test
    fun `invoke should schedule one-time alarm`() = runTest {
        // Given
        val oneTimeAlarm = testAlarm.copy(repeatDays = emptySet())
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(oneTimeAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify {
            alarmScheduler.scheduleAlarm(match { alarm ->
                alarm.repeatDays.isEmpty()
            })
        }
    }

    @Test
    fun `invoke should fail when repository update fails`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        verify(exactly = 0) { alarmScheduler.scheduleAlarm(any()) }
    }

    @Test
    fun `invoke should fail when trigger time update fails`() = runTest {
        // Given
        val exception = RuntimeException("Update error")
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        verify(exactly = 0) { alarmScheduler.scheduleAlarm(any()) }
    }

    @Test
    fun `invoke should fail when scheduler throws SecurityException`() = runTest {
        // Given
        val exception = SecurityException("Permission denied")
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(SecurityException::class.java)
    }

    @Test
    fun `invoke should schedule alarm with pre-alarms`() = runTest {
        // Given
        val alarmWithPreAlarms = testAlarm.copy(
            preAlarmCount = 3,
            preAlarmInterval = 5
        )
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(alarmWithPreAlarms)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify {
            alarmScheduler.scheduleAlarm(match { alarm ->
                alarm.preAlarmCount == 3 &&
                alarm.preAlarmInterval == 5
            })
        }
    }

    @Test
    fun `invoke should schedule alarm at specific time`() = runTest {
        // Given
        val morningAlarm = testAlarm.copy(time = LocalTime.of(6, 30))
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result = useCase(morningAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify {
            alarmScheduler.scheduleAlarm(match { alarm ->
                alarm.time == LocalTime.of(6, 30)
            })
        }
    }

    @Test
    fun `invoke should schedule multiple alarms sequentially`() = runTest {
        // Given
        val alarm1 = testAlarm.copy(id = 1)
        val alarm2 = testAlarm.copy(id = 2)
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        every { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When
        val result1 = useCase(alarm1)
        val result2 = useCase(alarm2)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        verify { alarmScheduler.scheduleAlarm(alarm1) }
        verify { alarmScheduler.scheduleAlarm(alarm2) }
    }

    // ============================================
    // CANCEL ALARM TESTS
    // ============================================

    @Test
    fun `cancel should cancel alarm successfully`() = runTest {
        // Given
        val alarmId = 1L
        every { alarmScheduler.cancelAlarm(alarmId) } returns Unit

        // When
        val result = useCase.cancel(alarmId)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { alarmScheduler.cancelAlarm(alarmId) }
    }

    @Test
    fun `cancel should cancel alarm with different ID`() = runTest {
        // Given
        val alarmId = 42L
        every { alarmScheduler.cancelAlarm(alarmId) } returns Unit

        // When
        val result = useCase.cancel(alarmId)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { alarmScheduler.cancelAlarm(42L) }
    }

    @Test
    fun `cancel should fail when scheduler throws exception`() = runTest {
        // Given
        val alarmId = 1L
        val exception = RuntimeException("Cancel failed")
        every { alarmScheduler.cancelAlarm(alarmId) } throws exception

        // When
        val result = useCase.cancel(alarmId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `cancel should handle multiple cancellations`() = runTest {
        // Given
        every { alarmScheduler.cancelAlarm(any()) } returns Unit

        // When
        val result1 = useCase.cancel(1L)
        val result2 = useCase.cancel(2L)
        val result3 = useCase.cancel(3L)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        assertThat(result3.isSuccess).isTrue()
        verify { alarmScheduler.cancelAlarm(1L) }
        verify { alarmScheduler.cancelAlarm(2L) }
        verify { alarmScheduler.cancelAlarm(3L) }
    }

    @Test
    fun `cancel should handle alarm with ID zero`() = runTest {
        // Given
        every { alarmScheduler.cancelAlarm(0L) } returns Unit

        // When
        val result = useCase.cancel(0L)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { alarmScheduler.cancelAlarm(0L) }
    }
}
