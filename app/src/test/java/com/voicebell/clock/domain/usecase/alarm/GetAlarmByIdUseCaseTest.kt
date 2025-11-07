package com.voicebell.clock.domain.usecase.alarm

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.AlarmTone
import com.voicebell.clock.domain.model.DayOfWeek
import com.voicebell.clock.domain.repository.AlarmRepository
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
 * Unit tests for GetAlarmByIdUseCase.
 */
class GetAlarmByIdUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var useCase: GetAlarmByIdUseCase

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
        useCase = GetAlarmByIdUseCase(alarmRepository)
    }

    // ============================================
    // FLOW TESTS (reactive)
    // ============================================

    @Test
    fun `invoke should return alarm as Flow`() = runTest {
        // Given
        every { alarmRepository.getAlarmByIdFlow(1) } returns flowOf(testAlarm)

        // When
        val result = useCase(1)

        // Then
        result.test {
            val alarm = awaitItem()
            assertThat(alarm).isNotNull()
            assertThat(alarm?.id).isEqualTo(1)
            assertThat(alarm?.label).isEqualTo("Morning Alarm")
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return null when alarm not found as Flow`() = runTest {
        // Given
        every { alarmRepository.getAlarmByIdFlow(999) } returns flowOf(null)

        // When
        val result = useCase(999)

        // Then
        result.test {
            val alarm = awaitItem()
            assertThat(alarm).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should emit updated alarm values as Flow`() = runTest {
        // Given - alarm gets updated during observation
        val updatedAlarm = testAlarm.copy(label = "Updated Label", isEnabled = false)
        every { alarmRepository.getAlarmByIdFlow(1) } returns flowOf(testAlarm, updatedAlarm)

        // When
        val result = useCase(1)

        // Then
        result.test {
            val alarm1 = awaitItem()
            assertThat(alarm1?.label).isEqualTo("Morning Alarm")
            assertThat(alarm1?.isEnabled).isTrue()

            val alarm2 = awaitItem()
            assertThat(alarm2?.label).isEqualTo("Updated Label")
            assertThat(alarm2?.isEnabled).isFalse()

            awaitComplete()
        }
    }

    @Test
    fun `invoke should return alarm with all properties as Flow`() = runTest {
        // Given
        val complexAlarm = testAlarm.copy(
            time = LocalTime.of(8, 30),
            repeatDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY
            ),
            alarmTone = AlarmTone.GENTLE,
            vibrate = false,
            gradualVolumeIncrease = false,
            snoozeEnabled = false,
            preAlarmCount = 3,
            preAlarmInterval = 7,
            volumeLevel = 75,
            flash = true
        )
        every { alarmRepository.getAlarmByIdFlow(2) } returns flowOf(complexAlarm)

        // When
        val result = useCase(2)

        // Then
        result.test {
            val alarm = awaitItem()
            assertThat(alarm).isNotNull()
            assertThat(alarm?.time).isEqualTo(LocalTime.of(8, 30))
            assertThat(alarm?.repeatDays).hasSize(3)
            assertThat(alarm?.alarmTone).isEqualTo(AlarmTone.GENTLE)
            assertThat(alarm?.vibrate).isFalse()
            assertThat(alarm?.gradualVolumeIncrease).isFalse()
            assertThat(alarm?.snoozeEnabled).isFalse()
            assertThat(alarm?.preAlarmCount).isEqualTo(3)
            assertThat(alarm?.preAlarmInterval).isEqualTo(7)
            assertThat(alarm?.volumeLevel).isEqualTo(75)
            assertThat(alarm?.flash).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return disabled alarm as Flow`() = runTest {
        // Given
        val disabledAlarm = testAlarm.copy(isEnabled = false)
        every { alarmRepository.getAlarmByIdFlow(1) } returns flowOf(disabledAlarm)

        // When
        val result = useCase(1)

        // Then
        result.test {
            val alarm = awaitItem()
            assertThat(alarm?.isEnabled).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return one-time alarm as Flow`() = runTest {
        // Given
        val oneTimeAlarm = testAlarm.copy(repeatDays = emptySet())
        every { alarmRepository.getAlarmByIdFlow(1) } returns flowOf(oneTimeAlarm)

        // When
        val result = useCase(1)

        // Then
        result.test {
            val alarm = awaitItem()
            assertThat(alarm?.repeatDays).isEmpty()
            awaitComplete()
        }
    }

    // ============================================
    // SINGLE READ TESTS (getOnce)
    // ============================================

    @Test
    fun `getOnce should return alarm by ID`() = runTest {
        // Given
        coEvery { alarmRepository.getAlarmById(1) } returns testAlarm

        // When
        val result = useCase.getOnce(1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1)
        assertThat(result?.label).isEqualTo("Morning Alarm")
        coVerify { alarmRepository.getAlarmById(1) }
    }

    @Test
    fun `getOnce should return null when alarm not found`() = runTest {
        // Given
        coEvery { alarmRepository.getAlarmById(999) } returns null

        // When
        val result = useCase.getOnce(999)

        // Then
        assertThat(result).isNull()
        coVerify { alarmRepository.getAlarmById(999) }
    }

    @Test
    fun `getOnce should return alarm with all properties`() = runTest {
        // Given
        val complexAlarm = testAlarm.copy(
            time = LocalTime.of(23, 59),
            repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            label = "Weekend Alarm",
            alarmTone = AlarmTone.DIGITAL,
            vibrate = true,
            gradualVolumeIncrease = true,
            flash = true
        )
        coEvery { alarmRepository.getAlarmById(5) } returns complexAlarm

        // When
        val result = useCase.getOnce(5)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.time).isEqualTo(LocalTime.of(23, 59))
        assertThat(result?.repeatDays).containsExactly(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        assertThat(result?.label).isEqualTo("Weekend Alarm")
        assertThat(result?.alarmTone).isEqualTo(AlarmTone.DIGITAL)
        assertThat(result?.vibrate).isTrue()
        assertThat(result?.gradualVolumeIncrease).isTrue()
        assertThat(result?.flash).isTrue()
    }

    @Test
    fun `getOnce should return disabled alarm`() = runTest {
        // Given
        val disabledAlarm = testAlarm.copy(isEnabled = false)
        coEvery { alarmRepository.getAlarmById(1) } returns disabledAlarm

        // When
        val result = useCase.getOnce(1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.isEnabled).isFalse()
    }

    @Test
    fun `getOnce should return alarm with empty label`() = runTest {
        // Given
        val unlabeledAlarm = testAlarm.copy(label = "")
        coEvery { alarmRepository.getAlarmById(1) } returns unlabeledAlarm

        // When
        val result = useCase.getOnce(1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.label).isEmpty()
    }

    @Test
    fun `getOnce should return alarm with snooze settings`() = runTest {
        // Given
        val alarmWithSnooze = testAlarm.copy(
            snoozeEnabled = true,
            snoozeDuration = 10,
            maxSnoozeCount = 5
        )
        coEvery { alarmRepository.getAlarmById(1) } returns alarmWithSnooze

        // When
        val result = useCase.getOnce(1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.snoozeEnabled).isTrue()
        assertThat(result?.snoozeDuration).isEqualTo(10)
        assertThat(result?.maxSnoozeCount).isEqualTo(5)
    }

    @Test
    fun `getOnce should return alarm with pre-alarm settings`() = runTest {
        // Given
        val alarmWithPreAlarm = testAlarm.copy(
            preAlarmCount = 3,
            preAlarmInterval = 7
        )
        coEvery { alarmRepository.getAlarmById(1) } returns alarmWithPreAlarm

        // When
        val result = useCase.getOnce(1)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.preAlarmCount).isEqualTo(3)
        assertThat(result?.preAlarmInterval).isEqualTo(7)
    }

    @Test
    fun `getOnce should handle different alarm IDs`() = runTest {
        // Given
        val alarm1 = testAlarm.copy(id = 1, label = "Alarm 1")
        val alarm2 = testAlarm.copy(id = 2, label = "Alarm 2")
        val alarm3 = testAlarm.copy(id = 3, label = "Alarm 3")
        coEvery { alarmRepository.getAlarmById(1) } returns alarm1
        coEvery { alarmRepository.getAlarmById(2) } returns alarm2
        coEvery { alarmRepository.getAlarmById(3) } returns alarm3

        // When
        val result1 = useCase.getOnce(1)
        val result2 = useCase.getOnce(2)
        val result3 = useCase.getOnce(3)

        // Then
        assertThat(result1?.label).isEqualTo("Alarm 1")
        assertThat(result2?.label).isEqualTo("Alarm 2")
        assertThat(result3?.label).isEqualTo("Alarm 3")
    }
}
