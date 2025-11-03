package com.voicebell.clock.domain.usecase.timer

import com.voicebell.clock.domain.repository.TimerRepository
import javax.inject.Inject

/**
 * Use case for stopping (canceling) a timer.
 * This marks the timer as finished and stops the countdown.
 */
class StopTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    suspend operator fun invoke(timerId: Long): Result<Unit> {
        return try {
            val timer = timerRepository.getTimerById(timerId)
                ?: return Result.failure(IllegalArgumentException("Timer not found"))

            // Mark as finished
            timerRepository.markAsFinished(timerId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
