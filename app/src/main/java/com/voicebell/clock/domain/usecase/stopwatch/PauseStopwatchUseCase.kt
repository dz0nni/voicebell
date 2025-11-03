package com.voicebell.clock.domain.usecase.stopwatch

import com.voicebell.clock.domain.repository.StopwatchRepository
import javax.inject.Inject

/**
 * Use case for pausing the stopwatch.
 */
class PauseStopwatchUseCase @Inject constructor(
    private val stopwatchRepository: StopwatchRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val currentState = stopwatchRepository.getStopwatchStateOnce()
                ?: return Result.failure(IllegalStateException("Stopwatch not started"))

            if (!currentState.isRunning || currentState.isPaused) {
                return Result.failure(IllegalStateException("Stopwatch is not running"))
            }

            // Calculate elapsed time up to this point
            val elapsedSinceLast = System.currentTimeMillis() - currentState.startTime
            val totalElapsed = currentState.pausedElapsedMillis + elapsedSinceLast

            val pausedState = currentState.copy(
                isRunning = false,
                isPaused = true,
                pausedTime = System.currentTimeMillis(),
                pausedElapsedMillis = totalElapsed,
                lastUpdated = System.currentTimeMillis()
            )

            stopwatchRepository.saveStopwatchState(pausedState)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
