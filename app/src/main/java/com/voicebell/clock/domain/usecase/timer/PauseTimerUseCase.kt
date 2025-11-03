package com.voicebell.clock.domain.usecase.timer

import com.voicebell.clock.domain.repository.TimerRepository
import javax.inject.Inject

/**
 * Use case for pausing a running timer.
 */
class PauseTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    suspend operator fun invoke(timerId: Long): Result<Unit> {
        return try {
            val timer = timerRepository.getTimerById(timerId)
                ?: return Result.failure(IllegalArgumentException("Timer not found"))

            if (!timer.isRunning || timer.isPaused) {
                return Result.failure(IllegalStateException("Timer is not running"))
            }

            // Calculate current remaining time
            val currentRemaining = timer.getCurrentRemainingMillis()

            val updatedTimer = timer.copy(
                isRunning = false,
                isPaused = true,
                remainingMillis = currentRemaining,
                pauseTime = System.currentTimeMillis()
            )

            timerRepository.updateTimer(updatedTimer)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
