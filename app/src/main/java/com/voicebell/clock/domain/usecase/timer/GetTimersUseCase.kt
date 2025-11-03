package com.voicebell.clock.domain.usecase.timer

import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all timers.
 */
class GetTimersUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    operator fun invoke(): Flow<List<Timer>> {
        return timerRepository.getAllTimers()
    }
}
