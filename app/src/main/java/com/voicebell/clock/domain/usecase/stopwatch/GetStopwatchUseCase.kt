package com.voicebell.clock.domain.usecase.stopwatch

import com.voicebell.clock.domain.model.Stopwatch
import com.voicebell.clock.domain.repository.StopwatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting stopwatch state.
 */
class GetStopwatchUseCase @Inject constructor(
    private val stopwatchRepository: StopwatchRepository
) {
    operator fun invoke(): Flow<Stopwatch?> {
        return stopwatchRepository.getStopwatchState()
    }

    suspend fun getOnce(): Stopwatch? {
        return stopwatchRepository.getStopwatchStateOnce()
    }
}
