package com.voicebell.clock.domain.usecase.timer

import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting active (running or paused) timers.
 */
class GetActiveTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    operator fun invoke(): Flow<List<Timer>> {
        return timerRepository.getActiveTimers()
    }

    /**
     * Get currently running timer (if any).
     */
    suspend fun getRunning(): Timer? {
        return timerRepository.getRunningTimer()
    }
}
