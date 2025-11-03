package com.voicebell.clock.data.repository

import com.voicebell.clock.data.local.database.dao.TimerDao
import com.voicebell.clock.data.mapper.toDomain
import com.voicebell.clock.data.mapper.toEntity
import com.voicebell.clock.domain.model.Timer
import com.voicebell.clock.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of TimerRepository.
 */
class TimerRepositoryImpl @Inject constructor(
    private val timerDao: TimerDao
) : TimerRepository {

    override fun getAllTimers(): Flow<List<Timer>> {
        return timerDao.getAllTimers().map { entities ->
            entities.toDomain()
        }
    }

    override fun getActiveTimers(): Flow<List<Timer>> {
        return timerDao.getActiveTimers().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getTimerById(id: Long): Timer? {
        return timerDao.getTimerById(id)?.toDomain()
    }

    override fun getTimerByIdFlow(id: Long): Flow<Timer?> {
        return timerDao.getTimerByIdFlow(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getRunningTimer(): Timer? {
        return timerDao.getRunningTimer()?.toDomain()
    }

    override suspend fun createTimer(timer: Timer): Long {
        return timerDao.insert(timer.toEntity())
    }

    override suspend fun updateTimer(timer: Timer) {
        timerDao.update(timer.toEntity())
    }

    override suspend fun deleteTimer(id: Long) {
        timerDao.deleteById(id)
    }

    override suspend fun deleteFinishedTimers() {
        timerDao.deleteFinishedTimers()
    }

    override suspend fun updateTimerState(id: Long, isRunning: Boolean, isPaused: Boolean) {
        timerDao.updateState(id, isRunning, isPaused)
    }

    override suspend fun updateRemainingTime(id: Long, remainingMillis: Long) {
        timerDao.updateRemainingTime(id, remainingMillis)
    }

    override suspend fun markAsFinished(id: Long) {
        timerDao.markAsFinished(id)
    }

    override suspend fun getActiveTimerCount(): Int {
        return timerDao.getActiveTimerCount()
    }
}
