package com.voicebell.clock.domain.usecase.worldclock

import com.voicebell.clock.domain.model.WorldClock
import com.voicebell.clock.domain.repository.WorldClockRepository
import javax.inject.Inject

/**
 * Use case for adding a new world clock.
 */
class AddWorldClockUseCase @Inject constructor(
    private val worldClockRepository: WorldClockRepository
) {
    suspend operator fun invoke(worldClock: WorldClock): Result<Long> {
        return try {
            // Check if timezone already exists
            if (worldClockRepository.timeZoneExists(worldClock.timeZoneId)) {
                return Result.failure(
                    IllegalArgumentException("This timezone already exists")
                )
            }

            val id = worldClockRepository.createWorldClock(worldClock)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
