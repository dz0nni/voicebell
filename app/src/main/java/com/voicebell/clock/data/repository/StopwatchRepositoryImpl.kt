package com.voicebell.clock.data.repository

import com.voicebell.clock.data.local.database.dao.StopwatchDao
import com.voicebell.clock.data.mapper.toDomain
import com.voicebell.clock.data.mapper.toEntity
import com.voicebell.clock.domain.model.Stopwatch
import com.voicebell.clock.domain.repository.StopwatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of StopwatchRepository.
 */
class StopwatchRepositoryImpl @Inject constructor(
    private val stopwatchDao: StopwatchDao
) : StopwatchRepository {

    override fun getStopwatchState(): Flow<Stopwatch?> {
        return stopwatchDao.getStopwatchState().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getStopwatchStateOnce(): Stopwatch? {
        return stopwatchDao.getStopwatchStateOnce()?.toDomain()
    }

    override suspend fun saveStopwatchState(stopwatch: Stopwatch) {
        stopwatchDao.insertOrUpdate(stopwatch.toEntity())
    }

    override suspend fun resetStopwatch() {
        stopwatchDao.reset()
    }
}
