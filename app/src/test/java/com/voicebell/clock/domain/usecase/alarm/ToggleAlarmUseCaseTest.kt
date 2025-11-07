package com.voicebell.clock.domain.usecase.alarm

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Unit tests for ToggleAlarmUseCase.
 */
class ToggleAlarmUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var useCase: ToggleAlarmUseCase

    private val testAlarm = Alarm(
        id = 1,
        time = LocalTime.of(7, 0),
        isEnabled = false,
        label = "Test Alarm",
        repeatDays = setOf(
            com.voicebell.clock.domain.model.DayOfWeek.MONDAY,
            com.voicebell.clock.domain.model.DayOfWeek.TUESDAY,
            com.voicebell.clock.domain.model.DayOfWeek.WEDNESDAY
        )
    )

    @Before
    fun setup() {
        alarmRepository = mockk()
        useCase = ToggleAlarmUseCase(alarmRepository)
    }

    @Test
    fun `invoke with enabled true should enable alarm and calculate next trigger`() = runTest {
        // Given
        coEvery { alarmRepository.toggleAlarmEnabled(any(), any()) } returns Unit
        coEvery { alarmRepository.getAlarmById(1) } returns testAlarm.copy(isEnabled = true)
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(alarmId = 1, isEnabled = true)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.toggleAlarmEnabled(1, true) }
        coVerify { alarmRepository.getAlarmById(1) }
        coVerify { alarmRepository.updateNextTriggerTime(1, any()) }
        coVerify { alarmRepository.resetSnoozeCount(1) }
    }

    @Test
    fun `invoke with enabled false should disable alarm and set trigger to zero`() = runTest {
        // Given
        coEvery { alarmRepository.toggleAlarmEnabled(any(), any()) } returns Unit
        coEvery { alarmRepository.getAlarmById(1) } returns testAlarm.copy(isEnabled = false)
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        val result = useCase(alarmId = 1, isEnabled = false)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.toggleAlarmEnabled(1, false) }
        coVerify { alarmRepository.updateNextTriggerTime(1, 0) }
        coVerify { alarmRepository.resetSnoozeCount(1) }
    }

    @Test
    fun `invoke should return failure when alarm not found`() = runTest {
        // Given
        coEvery { alarmRepository.toggleAlarmEnabled(any(), any()) } returns Unit
        coEvery { alarmRepository.getAlarmById(1) } returns null

        // When
        val result = useCase(alarmId = 1, isEnabled = true)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Alarm not found")
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { alarmRepository.toggleAlarmEnabled(any(), any()) } throws exception

        // When
        val result = useCase(alarmId = 1, isEnabled = true)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should reset snooze count when toggling`() = runTest {
        // Given
        coEvery { alarmRepository.toggleAlarmEnabled(any(), any()) } returns Unit
        coEvery { alarmRepository.getAlarmById(1) } returns testAlarm
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When
        useCase(alarmId = 1, isEnabled = false)

        // Then
        coVerify { alarmRepository.resetSnoozeCount(1) }
    }

    @Test
    fun `invoke should handle multiple toggles correctly`() = runTest {
        // Given
        coEvery { alarmRepository.toggleAlarmEnabled(any(), any()) } returns Unit
        coEvery { alarmRepository.getAlarmById(1) } returns testAlarm
        coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit
        coEvery { alarmRepository.resetSnoozeCount(any()) } returns Unit

        // When - toggle on then off
        val result1 = useCase(alarmId = 1, isEnabled = true)
        val result2 = useCase(alarmId = 1, isEnabled = false)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        coVerify(exactly = 2) { alarmRepository.toggleAlarmEnabled(1, any()) }
        coVerify(exactly = 2) { alarmRepository.resetSnoozeCount(1) }
    }
}
