package com.voicebell.clock.data.local.database.dao

import androidx.room.*
import com.voicebell.clock.data.local.database.entities.AlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for alarm database operations.
 *
 * All operations are suspending functions or return Flow for reactive updates.
 */
@Dao
interface AlarmDao {

    /**
     * Get all alarms as a Flow (reactive).
     * Ordered by time (hour, then minute).
     */
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    /**
     * Get all enabled alarms.
     */
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    fun getEnabledAlarms(): Flow<List<AlarmEntity>>

    /**
     * Get alarm by ID.
     */
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    /**
     * Get alarm by ID as Flow.
     */
    @Query("SELECT * FROM alarms WHERE id = :id")
    fun getAlarmByIdFlow(id: Long): Flow<AlarmEntity?>

    /**
     * Get next scheduled alarm (earliest enabled alarm).
     */
    @Query("""
        SELECT * FROM alarms
        WHERE isEnabled = 1 AND nextTriggerTime > 0
        ORDER BY nextTriggerTime ASC
        LIMIT 1
    """)
    suspend fun getNextScheduledAlarm(): AlarmEntity?

    /**
     * Insert new alarm. Returns the auto-generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    /**
     * Insert multiple alarms.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alarms: List<AlarmEntity>)

    /**
     * Update existing alarm.
     */
    @Update
    suspend fun update(alarm: AlarmEntity)

    /**
     * Delete alarm.
     */
    @Delete
    suspend fun delete(alarm: AlarmEntity)

    /**
     * Delete alarm by ID.
     */
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Delete all alarms.
     */
    @Query("DELETE FROM alarms")
    suspend fun deleteAll()

    /**
     * Toggle alarm enabled state.
     */
    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: Long, isEnabled: Boolean)

    /**
     * Update next trigger time.
     */
    @Query("UPDATE alarms SET nextTriggerTime = :triggerTime WHERE id = :id")
    suspend fun updateNextTriggerTime(id: Long, triggerTime: Long)

    /**
     * Update snooze count.
     */
    @Query("UPDATE alarms SET snoozeCount = :count WHERE id = :id")
    suspend fun updateSnoozeCount(id: Long, count: Int)

    /**
     * Reset snooze count to 0.
     */
    @Query("UPDATE alarms SET snoozeCount = 0 WHERE id = :id")
    suspend fun resetSnoozeCount(id: Long)

    /**
     * Get count of enabled alarms.
     */
    @Query("SELECT COUNT(*) FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarmCount(): Int

    /**
     * Get count of all alarms.
     */
    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getTotalAlarmCount(): Int
}
