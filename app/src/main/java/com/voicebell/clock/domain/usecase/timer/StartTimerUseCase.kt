package com.voicebell.clock.domain.usecase.timer

import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.repository.TimerRepository
import javax.inject.Inject

/**
 * Use case for starting a new timer or resuming a paused one.
 */
class StartTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    /**
     * Start a new timer with given duration.
     * @param durationMillis Duration in milliseconds
     * @param label Optional label for the timer
     * @return Timer ID
     */
    suspend operator fun invoke(
        durationMillis: Long,
        label: String = "",
        vibrate: Boolean = true
    ): Result<Long> {
        return try {
            if (durationMillis <= 0) {
                return Result.failure(IllegalArgumentException("Duration must be positive"))
            }

            // Check if there's already a running timer
            val runningTimer = timerRepository.getRunningTimer()
            if (runningTimer != null) {
                return Result.failure(
                    IllegalStateException("Cannot start timer: Timer ${runningTimer.id} is already running")
                )
            }

            val timer = Timer(
                label = label,
                durationMillis = durationMillis,
                remainingMillis = durationMillis,
                isRunning = true,
                isPaused = false,
                isFinished = false,
                startTime = System.currentTimeMillis(),
                pauseTime = 0,
                endTime = System.currentTimeMillis() + durationMillis,
                vibrate = vibrate
            )

            val timerId = timerRepository.createTimer(timer)
            Result.success(timerId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resume a paused timer.
     */
    suspend fun resume(timerId: Long): Result<Unit> {
        return try {
            val timer = timerRepository.getTimerById(timerId)
                ?: return Result.failure(IllegalArgumentException("Timer not found"))

            if (!timer.isPaused) {
                return Result.failure(IllegalStateException("Timer is not paused"))
            }

            val updatedTimer = timer.copy(
                isRunning = true,
                isPaused = false,
                startTime = System.currentTimeMillis()
            )

            timerRepository.updateTimer(updatedTimer)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
