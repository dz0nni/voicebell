package com.voicebell.clock.domain.usecase.worldclock

import com.voicebell.clock.domain.model.WorldClock
import com.voicebell.clock.domain.repository.WorldClockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all world clocks.
 */
class GetWorldClocksUseCase @Inject constructor(
    private val worldClockRepository: WorldClockRepository
) {
    operator fun invoke(): Flow<List<WorldClock>> {
        return worldClockRepository.getAllWorldClocks()
    }
}
