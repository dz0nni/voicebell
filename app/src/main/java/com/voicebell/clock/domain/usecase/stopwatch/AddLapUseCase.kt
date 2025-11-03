package com.voicebell.clock.domain.usecase.stopwatch

import com.voicebell.clock.domain.model.Stopwatch
import com.voicebell.clock.domain.repository.StopwatchRepository
import javax.inject.Inject

/**
 * Use case for adding a lap to the stopwatch.
 */
class AddLapUseCase @Inject constructor(
    private val stopwatchRepository: StopwatchRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val currentState = stopwatchRepository.getStopwatchStateOnce()
                ?: return Result.failure(IllegalStateException("Stopwatch not started"))

            if (!currentState.isRunning || currentState.isPaused) {
                return Result.failure(IllegalStateException("Stopwatch must be running to add lap"))
            }

            // Calculate current total time
            val totalTime = currentState.getCurrentElapsedMillis()

            // Calculate lap time (time since last lap)
            val lapTime = currentState.getCurrentLapTime()

            // Create new lap
            val newLap = Stopwatch.Lap(
                lapNumber = currentState.laps.size + 1,
                lapTime = lapTime,
                totalTime = totalTime
            )

            // Add lap to list
            val updatedState = currentState.copy(
                laps = currentState.laps + newLap,
                lastUpdated = System.currentTimeMillis()
            )

            stopwatchRepository.saveStopwatchState(updatedState)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
