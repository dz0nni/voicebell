package com.voicebell.clock.domain.usecase.alarm

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.AlarmTone
import com.voicebell.clock.domain.model.DayOfWeek
import com.voicebell.clock.domain.repository.AlarmRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Unit tests for UpdateAlarmUseCase.
 */
class UpdateAlarmUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var useCase: UpdateAlarmUseCase

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
        useCase = UpdateAlarmUseCase(alarmRepository)
    }

    @Test
    fun `invoke should update alarm and recalculate trigger time`() = runTest {
        // Given
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.updateAlarm(testAlarm) }
        coVerify { alarmRepository.updateNextTriggerTime(1, any()) }
        coVerify { alarmRepository.resetSnoozeCount(1) }
    }

    @Test
    fun `invoke should set trigger time to 0 when alarm is disabled`() = runTest {
        // Given
        val disabledAlarm = testAlarm.copy(isEnabled = false)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(disabledAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.updateNextTriggerTime(1, 0) }
    }

    @Test
    fun `invoke should reset snooze count when alarm is updated`() = runTest {
        // Given
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.resetSnoozeCount(1) }
    }

    @Test
    fun `invoke should update alarm with changed time`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(time = LocalTime.of(8, 30))
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.time == LocalTime.of(8, 30)
            })
        }
    }

    @Test
    fun `invoke should update alarm with changed repeat days`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(
            repeatDays = setOf(
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            )
        )
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.repeatDays.contains(DayOfWeek.SATURDAY) &&
                alarm.repeatDays.contains(DayOfWeek.SUNDAY) &&
                !alarm.repeatDays.contains(DayOfWeek.MONDAY)
            })
        }
    }

    @Test
    fun `invoke should update alarm label`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(label = "Updated Label")
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.label == "Updated Label"
            })
        }
    }

    @Test
    fun `invoke should update alarm tone`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(alarmTone = AlarmTone.GENTLE)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.alarmTone == AlarmTone.GENTLE
            })
        }
    }

    @Test
    fun `invoke should update vibrate setting`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(vibrate = false)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                !alarm.vibrate
            })
        }
    }

    @Test
    fun `invoke should update gradual volume increase setting`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(gradualVolumeIncrease = false)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                !alarm.gradualVolumeIncrease
            })
        }
    }

    @Test
    fun `invoke should update snooze settings`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(
            snoozeEnabled = false,
            snoozeDuration = 10,
            maxSnoozeCount = 5
        )
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                !alarm.snoozeEnabled &&
                alarm.snoozeDuration == 10 &&
                alarm.maxSnoozeCount == 5
            })
        }
    }

    @Test
    fun `invoke should update pre-alarm settings`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(
            preAlarmCount = 3,
            preAlarmInterval = 10
        )
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.preAlarmCount == 3 &&
                alarm.preAlarmInterval == 10
            })
        }
    }

    @Test
    fun `invoke should update volume level`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(volumeLevel = 75)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.volumeLevel == 75
            })
        }
    }

    @Test
    fun `invoke should update flash setting`() = runTest {
        // Given
        val updatedAlarm = testAlarm.copy(flash = true)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(updatedAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.flash
            })
        }
    }

    @Test
    fun `invoke should convert one-time alarm to repeating`() = runTest {
        // Given
        val oneTimeAlarm = testAlarm.copy(repeatDays = emptySet())
        val repeatingAlarm = oneTimeAlarm.copy(
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(repeatingAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.repeatDays.size == 3
            })
        }
    }

    @Test
    fun `invoke should convert repeating alarm to one-time`() = runTest {
        // Given
        val oneTimeAlarm = testAlarm.copy(repeatDays = emptySet())
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(oneTimeAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.repeatDays.isEmpty()
            })
        }
    }

    @Test
    fun `invoke should fail when repository throws exception on update`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { alarmRepository.updateAlarm(any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 0) { alarmRepository.updateNextTriggerTime(any(), any()) }
        coVerify(exactly = 0) { alarmRepository.resetSnoozeCount(any()) }
    }

    @Test
    fun `invoke should fail when repository throws exception on trigger time update`() = runTest {
        // Given
        val exception = RuntimeException("Trigger update error")
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should fail when repository throws exception on snooze reset`() = runTest {
        // Given
        val exception = RuntimeException("Snooze reset error")
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should enable previously disabled alarm`() = runTest {
        // Given
        val disabledAlarm = testAlarm.copy(isEnabled = false)
        val enabledAlarm = disabledAlarm.copy(isEnabled = true)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(enabledAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                alarm.isEnabled
            })
        }
        coVerify { alarmRepository.updateNextTriggerTime(1, match { time -> time > 0 }) }
    }

    @Test
    fun `invoke should disable previously enabled alarm`() = runTest {
        // Given
        val disabledAlarm = testAlarm.copy(isEnabled = false)
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(disabledAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateAlarm(match { alarm ->
                !alarm.isEnabled
            })
        }
        coVerify { alarmRepository.updateNextTriggerTime(1, 0) }
    }
}
