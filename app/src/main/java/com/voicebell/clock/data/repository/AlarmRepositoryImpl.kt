package com.voicebell.clock.data.repository

import com.voicebell.clock.data.local.database.dao.AlarmDao
import com.voicebell.clock.data.mapper.toDomain
import com.voicebell.clock.data.mapper.toEntity
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of AlarmRepository.
 *
 * Handles data operations for alarms using Room database.
 */
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.toDomain()
        }
    }

    override fun getEnabledAlarms(): Flow<List<Alarm>> {
        return alarmDao.getEnabledAlarms().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    override fun getAlarmByIdFlow(id: Long): Flow<Alarm?> {
        return alarmDao.getAlarmByIdFlow(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getNextScheduledAlarm(): Alarm? {
        return alarmDao.getNextScheduledAlarm()?.toDomain()
    }

    override suspend fun createAlarm(alarm: Alarm): Long {
        return alarmDao.insert(alarm.toEntity())
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.update(alarm.toEntity())
    }

    override suspend fun deleteAlarm(id: Long) {
        alarmDao.deleteById(id)
    }

    override suspend fun toggleAlarmEnabled(id: Long, isEnabled: Boolean) {
        alarmDao.setEnabled(id, isEnabled)
    }

    override suspend fun updateNextTriggerTime(id: Long, triggerTime: Long) {
        alarmDao.updateNextTriggerTime(id, triggerTime)
    }

    override suspend fun updateSnoozeCount(id: Long, count: Int) {
        alarmDao.updateSnoozeCount(id, count)
    }

    override suspend fun resetSnoozeCount(id: Long) {
        alarmDao.resetSnoozeCount(id)
    }

    override suspend fun getEnabledAlarmCount(): Int {
        return alarmDao.getEnabledAlarmCount()
    }

    override suspend fun getTotalAlarmCount(): Int {
        return alarmDao.getTotalAlarmCount()
    }
}
