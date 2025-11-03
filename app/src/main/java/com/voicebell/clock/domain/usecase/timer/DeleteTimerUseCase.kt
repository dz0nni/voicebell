package com.voicebell.clock.domain.usecase.timer

import com.voicebell.clock.domain.repository.TimerRepository
import javax.inject.Inject

/**
 * Use case for deleting a timer.
 */
class DeleteTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    suspend operator fun invoke(timerId: Long): Result<Unit> {
        return try {
            timerRepository.deleteTimer(timerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
