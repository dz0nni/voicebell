package com.voicebell.clock.domain.usecase.stopwatch

import com.voicebell.clock.domain.model.Stopwatch
import com.voicebell.clock.domain.repository.StopwatchRepository
import javax.inject.Inject

/**
 * Use case for starting the stopwatch.
 */
class StartStopwatchUseCase @Inject constructor(
    private val stopwatchRepository: StopwatchRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val currentState = stopwatchRepository.getStopwatchStateOnce()

            // If already running, do nothing
            if (currentState?.isRunning == true && !currentState.isPaused) {
                return Result.success(Unit)
            }

            val newState = if (currentState?.isPaused == true) {
                // Resume from paused state
                currentState.copy(
                    isRunning = true,
                    isPaused = false,
                    startTime = System.currentTimeMillis(),
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                // Start fresh
                Stopwatch(
                    isRunning = true,
                    isPaused = false,
                    startTime = System.currentTimeMillis(),
                    pausedTime = 0,
                    elapsedMillis = 0,
                    pausedElapsedMillis = 0,
                    laps = emptyList(),
                    lastUpdated = System.currentTimeMillis()
                )
            }

            stopwatchRepository.saveStopwatchState(newState)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
