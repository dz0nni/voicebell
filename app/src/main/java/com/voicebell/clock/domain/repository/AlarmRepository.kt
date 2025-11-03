package com.voicebell.clock.domain.repository

import com.voicebell.clock.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for alarm operations.
 *
 * This interface defines the contract for alarm data access.
 * Implementations are in the data layer.
 */
interface AlarmRepository {

    /**
     * Get all alarms as Flow (reactive).
     */
    fun getAllAlarms(): Flow<List<Alarm>>

    /**
     * Get all enabled alarms.
     */
    fun getEnabledAlarms(): Flow<List<Alarm>>

    /**
     * Get alarm by ID.
     */
    suspend fun getAlarmById(id: Long): Alarm?

    /**
     * Get alarm by ID as Flow.
     */
    fun getAlarmByIdFlow(id: Long): Flow<Alarm?>

    /**
     * Get next scheduled alarm.
     */
    suspend fun getNextScheduledAlarm(): Alarm?

    /**
     * Create new alarm. Returns the generated ID.
     */
    suspend fun createAlarm(alarm: Alarm): Long

    /**
     * Update existing alarm.
     */
    suspend fun updateAlarm(alarm: Alarm)

    /**
     * Delete alarm.
     */
    suspend fun deleteAlarm(id: Long)

    /**
     * Toggle alarm enabled state.
     */
    suspend fun toggleAlarmEnabled(id: Long, isEnabled: Boolean)

    /**
     * Update next trigger time.
     */
    suspend fun updateNextTriggerTime(id: Long, triggerTime: Long)

    /**
     * Update snooze count.
     */
    suspend fun updateSnoozeCount(id: Long, count: Int)

    /**
     * Reset snooze count to 0.
     */
    suspend fun resetSnoozeCount(id: Long)

    /**
     * Get count of enabled alarms.
     */
    suspend fun getEnabledAlarmCount(): Int

    /**
     * Get total alarm count.
     */
    suspend fun getTotalAlarmCount(): Int
}
