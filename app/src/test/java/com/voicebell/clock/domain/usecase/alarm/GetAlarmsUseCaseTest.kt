package com.voicebell.clock.domain.usecase.alarm

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Unit tests for GetAlarmsUseCase.
 */
class GetAlarmsUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var useCase: GetAlarmsUseCase

    private val testAlarm1 = Alarm(
        id = 1,
        time = LocalTime.of(7, 0),
        isEnabled = true,
        label = "Morning Alarm"
    )

    private val testAlarm2 = Alarm(
        id = 2,
        time = LocalTime.of(8, 30),
        isEnabled = false,
        label = "Backup Alarm"
    )

    @Before
    fun setup() {
        alarmRepository = mockk()
        useCase = GetAlarmsUseCase(alarmRepository)
    }

    @Test
    fun `invoke should return all alarms from repository`() = runTest {
        // Given
        val alarms = listOf(testAlarm1, testAlarm2)
        every { alarmRepository.getAllAlarms() } returns flowOf(alarms)

        // When
        val result = useCase()

        // Then
        result.test {
            val emittedAlarms = awaitItem()
            assertThat(emittedAlarms).hasSize(2)
            assertThat(emittedAlarms).containsExactly(testAlarm1, testAlarm2)
            awaitComplete()
        }

        verify { alarmRepository.getAllAlarms() }
    }

    @Test
    fun `invoke should return empty list when no alarms`() = runTest {
        // Given
        every { alarmRepository.getAllAlarms() } returns flowOf(emptyList())

        // When
        val result = useCase()

        // Then
        result.test {
            val emittedAlarms = awaitItem()
            assertThat(emittedAlarms).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getEnabled should return only enabled alarms`() = runTest {
        // Given
        val enabledAlarms = listOf(testAlarm1)
        every { alarmRepository.getEnabledAlarms() } returns flowOf(enabledAlarms)

        // When
        val result = useCase.getEnabled()

        // Then
        result.test {
            val emittedAlarms = awaitItem()
            assertThat(emittedAlarms).hasSize(1)
            assertThat(emittedAlarms.first().isEnabled).isTrue()
            awaitComplete()
        }

        verify { alarmRepository.getEnabledAlarms() }
    }

    @Test
    fun `invoke should emit new values when repository data changes`() = runTest {
        // Given
        val initialAlarms = listOf(testAlarm1)
        val updatedAlarms = listOf(testAlarm1, testAlarm2)
        every { alarmRepository.getAllAlarms() } returns flowOf(initialAlarms, updatedAlarms)

        // When
        val result = useCase()

        // Then
        result.test {
            val first = awaitItem()
            assertThat(first).hasSize(1)

            val second = awaitItem()
            assertThat(second).hasSize(2)

            awaitComplete()
        }
    }
}
