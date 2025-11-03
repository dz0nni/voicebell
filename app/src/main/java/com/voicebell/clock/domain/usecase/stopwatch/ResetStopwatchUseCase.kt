package com.voicebell.clock.domain.usecase.stopwatch

import com.voicebell.clock.domain.repository.StopwatchRepository
import javax.inject.Inject

/**
 * Use case for resetting the stopwatch to initial state.
 */
class ResetStopwatchUseCase @Inject constructor(
    private val stopwatchRepository: StopwatchRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            stopwatchRepository.resetStopwatch()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
