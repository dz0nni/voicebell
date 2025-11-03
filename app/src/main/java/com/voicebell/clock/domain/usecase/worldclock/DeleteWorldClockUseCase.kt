package com.voicebell.clock.domain.usecase.worldclock

import com.voicebell.clock.domain.repository.WorldClockRepository
import javax.inject.Inject

/**
 * Use case for deleting a world clock.
 */
class DeleteWorldClockUseCase @Inject constructor(
    private val worldClockRepository: WorldClockRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            worldClockRepository.deleteWorldClock(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
