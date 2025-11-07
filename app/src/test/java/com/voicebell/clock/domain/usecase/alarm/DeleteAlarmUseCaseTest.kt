package com.voicebell.clock.domain.usecase.alarm

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.repository.AlarmRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteAlarmUseCase.
 */
class DeleteAlarmUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var useCase: DeleteAlarmUseCase

    @Before
    fun setup() {
        alarmRepository = mockk()
        useCase = DeleteAlarmUseCase(alarmRepository)
    }

    @Test
    fun `invoke should delete alarm successfully`() = runTest {
        // Given
        val alarmId = 1L
        coEvery { alarmRepository.deleteAlarm(alarmId) } returns Unit

        // When
        val result = useCase(alarmId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.deleteAlarm(alarmId) }
    }

    @Test
    fun `invoke should delete alarm with different ID`() = runTest {
        // Given
        val alarmId = 42L
        coEvery { alarmRepository.deleteAlarm(alarmId) } returns Unit

        // When
        val result = useCase(alarmId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.deleteAlarm(42L) }
    }

    @Test
    fun `invoke should fail when repository throws exception`() = runTest {
        // Given
        val alarmId = 1L
        val exception = RuntimeException("Database error")
        coEvery { alarmRepository.deleteAlarm(alarmId) } throws exception

        // When
        val result = useCase(alarmId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should handle deletion of non-existent alarm gracefully`() = runTest {
        // Given - some DAOs don't throw for non-existent deletes
        val alarmId = 999L
        coEvery { alarmRepository.deleteAlarm(alarmId) } returns Unit

        // When
        val result = useCase(alarmId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.deleteAlarm(999L) }
    }

    @Test
    fun `invoke should delete multiple alarms sequentially`() = runTest {
        // Given
        coEvery { alarmRepository.deleteAlarm(any()) } returns Unit

        // When - delete multiple alarms
        val result1 = useCase(1L)
        val result2 = useCase(2L)
        val result3 = useCase(3L)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        assertThat(result3.isSuccess).isTrue()
        coVerify { alarmRepository.deleteAlarm(1L) }
        coVerify { alarmRepository.deleteAlarm(2L) }
        coVerify { alarmRepository.deleteAlarm(3L) }
    }

    @Test
    fun `invoke should propagate specific exception types`() = runTest {
        // Given
        val alarmId = 1L
        val exception = IllegalArgumentException("Invalid alarm ID")
        coEvery { alarmRepository.deleteAlarm(alarmId) } throws exception

        // When
        val result = useCase(alarmId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Invalid alarm ID")
    }

    @Test
    fun `invoke should delete alarm with ID zero`() = runTest {
        // Given - though unlikely, ID 0 should be handled
        val alarmId = 0L
        coEvery { alarmRepository.deleteAlarm(alarmId) } returns Unit

        // When
        val result = useCase(alarmId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.deleteAlarm(0L) }
    }

    @Test
    fun `invoke should delete alarm with large ID`() = runTest {
        // Given
        val alarmId = Long.MAX_VALUE
        coEvery { alarmRepository.deleteAlarm(alarmId) } returns Unit

        // When
        val result = useCase(alarmId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { alarmRepository.deleteAlarm(Long.MAX_VALUE) }
    }
}
