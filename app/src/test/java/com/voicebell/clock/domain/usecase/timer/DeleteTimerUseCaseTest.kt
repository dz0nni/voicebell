package com.voicebell.clock.domain.usecase.timer

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.repository.TimerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteTimerUseCase.
 */
class DeleteTimerUseCaseTest {

    private lateinit var timerRepository: TimerRepository
    private lateinit var useCase: DeleteTimerUseCase

    @Before
    fun setup() {
        timerRepository = mockk()
        useCase = DeleteTimerUseCase(timerRepository)
    }

    @Test
    fun `invoke should delete timer successfully`() = runTest {
        // Given
        val timerId = 1L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(timerId) }
    }

    @Test
    fun `invoke should delete timer with different ID`() = runTest {
        // Given
        val timerId = 42L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(42L) }
    }

    @Test
    fun `invoke should fail when repository throws exception`() = runTest {
        // Given
        val timerId = 1L
        val exception = RuntimeException("Database error")
        coEvery { timerRepository.deleteTimer(timerId) } throws exception

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should handle deletion of non-existent timer gracefully`() = runTest {
        // Given - some DAOs don't throw for non-existent deletes
        val timerId = 999L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(999L) }
    }

    @Test
    fun `invoke should delete multiple timers sequentially`() = runTest {
        // Given
        coEvery { timerRepository.deleteTimer(any()) } returns Unit

        // When - delete multiple timers
        val result1 = useCase(1L)
        val result2 = useCase(2L)
        val result3 = useCase(3L)

        // Then
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        assertThat(result3.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(1L) }
        coVerify { timerRepository.deleteTimer(2L) }
        coVerify { timerRepository.deleteTimer(3L) }
    }

    @Test
    fun `invoke should propagate specific exception types`() = runTest {
        // Given
        val timerId = 1L
        val exception = IllegalArgumentException("Invalid timer ID")
        coEvery { timerRepository.deleteTimer(timerId) } throws exception

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Invalid timer ID")
    }

    @Test
    fun `invoke should delete timer with ID zero`() = runTest {
        // Given - though unlikely, ID 0 should be handled
        val timerId = 0L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(0L) }
    }

    @Test
    fun `invoke should delete timer with large ID`() = runTest {
        // Given
        val timerId = Long.MAX_VALUE
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(Long.MAX_VALUE) }
    }

    @Test
    fun `invoke should delete running timer`() = runTest {
        // Given - running timer can be deleted
        val timerId = 5L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(5L) }
    }

    @Test
    fun `invoke should delete paused timer`() = runTest {
        // Given - paused timer can be deleted
        val timerId = 6L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(6L) }
    }

    @Test
    fun `invoke should delete finished timer`() = runTest {
        // Given - finished timer can be deleted
        val timerId = 7L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { timerRepository.deleteTimer(7L) }
    }

    @Test
    fun `invoke should handle database constraint errors`() = runTest {
        // Given
        val timerId = 1L
        val exception = RuntimeException("FOREIGN KEY constraint failed")
        coEvery { timerRepository.deleteTimer(timerId) } throws exception

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("FOREIGN KEY")
    }

    @Test
    fun `invoke should delete timer and return success result`() = runTest {
        // Given
        val timerId = 10L
        coEvery { timerRepository.deleteTimer(timerId) } returns Unit

        // When
        val result = useCase(timerId)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
    }
}
