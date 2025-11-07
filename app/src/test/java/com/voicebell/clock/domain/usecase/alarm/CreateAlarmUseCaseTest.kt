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
 * Unit tests for CreateAlarmUseCase.
 */
class CreateAlarmUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var useCase: CreateAlarmUseCase

    private val testAlarm = Alarm(
        id = 0, // New alarm
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
        useCase = CreateAlarmUseCase(alarmRepository)
    }

    @Test
    fun `invoke should create alarm and calculate next trigger time`() = runTest {
        // Given
        val expectedId = 42L
        coEvery { alarmRepository.createAlarm(any()) } returns expectedId
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedId)
        coVerify { alarmRepository.createAlarm(testAlarm) }
        coVerify { alarmRepository.updateNextTriggerTime(expectedId, any()) }
    }

    @Test
    fun `invoke should create enabled alarm with correct settings`() = runTest {
        // Given
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.isEnabled &&
                alarm.time == LocalTime.of(7, 0) &&
                alarm.label == "Morning Alarm" &&
                alarm.vibrate &&
                alarm.gradualVolumeIncrease
            })
        }
    }

    @Test
    fun `invoke should create alarm with repeat days`() = runTest {
        // Given
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.repeatDays.contains(DayOfWeek.MONDAY) &&
                alarm.repeatDays.contains(DayOfWeek.TUESDAY)
            })
        }
    }

    @Test
    fun `invoke should create one-time alarm without repeat days`() = runTest {
        // Given
        val oneTimeAlarm = testAlarm.copy(repeatDays = emptySet())
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(oneTimeAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.repeatDays.isEmpty()
            })
        }
    }

    @Test
    fun `invoke should create alarm with snooze settings`() = runTest {
        // Given
        val alarmWithSnooze = testAlarm.copy(
            snoozeEnabled = true,
            snoozeDuration = 10,
            maxSnoozeCount = 5
        )
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(alarmWithSnooze)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.snoozeEnabled &&
                alarm.snoozeDuration == 10 &&
                alarm.maxSnoozeCount == 5
            })
        }
    }

    @Test
    fun `invoke should update next trigger time after creation`() = runTest {
        // Given
        val alarmId = 99L
        coEvery { alarmRepository.createAlarm(any()) } returns alarmId
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.updateNextTriggerTime(
                alarmId,
                match { triggerTime -> triggerTime > 0 }
            )
        }
    }

    @Test
    fun `invoke should fail when repository throws exception on create`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { alarmRepository.createAlarm(any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 0) { alarmRepository.updateNextTriggerTime(any(), any()) }
    }

    @Test
    fun `invoke should fail when repository throws exception on update trigger`() = runTest {
        // Given
        val exception = RuntimeException("Update error")
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } throws exception

        // When
        val result = useCase(testAlarm)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should create alarm with pre-alarm settings`() = runTest {
        // Given
        val alarmWithPreAlarm = testAlarm.copy(
            preAlarmCount = 3,
            preAlarmInterval = 7
        )
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(alarmWithPreAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.preAlarmCount == 3 &&
                alarm.preAlarmInterval == 7
            })
        }
    }

    @Test
    fun `invoke should create alarm with custom volume`() = runTest {
        // Given
        val alarmWithVolume = testAlarm.copy(
            volumeLevel = 50
        )
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(alarmWithVolume)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.volumeLevel == 50
            })
        }
    }

    @Test
    fun `invoke should create alarm with flash enabled`() = runTest {
        // Given
        val alarmWithFlash = testAlarm.copy(
            flash = true
        )
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(alarmWithFlash)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.flash
            })
        }
    }

    @Test
    fun `invoke should create disabled alarm`() = runTest {
        // Given
        val disabledAlarm = testAlarm.copy(isEnabled = false)
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(disabledAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                !alarm.isEnabled
            })
        }
    }

    @Test
    fun `invoke should create alarm with all days of week`() = runTest {
        // Given
        val dailyAlarm = testAlarm.copy(
            repeatDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            )
        )
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(dailyAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.repeatDays.size == 7
            })
        }
    }

    @Test
    fun `invoke should create alarm with custom label`() = runTest {
        // Given
        val labeledAlarm = testAlarm.copy(
            label = "Custom Important Alarm ⏰"
        )
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(labeledAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.label == "Custom Important Alarm ⏰"
            })
        }
    }

    @Test
    fun `invoke should create alarm with empty label`() = runTest {
        // Given
        val unlabeledAlarm = testAlarm.copy(label = "")
        coEvery { alarmRepository.createAlarm(any()) } returns 1L
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

        // When
        val result = useCase(unlabeledAlarm)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            alarmRepository.createAlarm(match { alarm ->
                alarm.label.isEmpty()
            })
        }
    }
}
