package com.voicebell.clock.data.local.database.dao

import androidx.room.*
import com.voicebell.clock.data.local.database.entities.TimerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for timer database operations.
 */
@Dao
interface TimerDao {

    /**
     * Get all timers as Flow.
     * Ordered by creation time (newest first).
     */
    @Query("SELECT * FROM timers ORDER BY createdAt DESC")
    fun getAllTimers(): Flow<List<TimerEntity>>

    /**
     * Get all active (running or paused) timers.
     */
    @Query("SELECT * FROM timers WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getActiveTimers(): Flow<List<TimerEntity>>

    /**
     * Get timer by ID.
     */
    @Query("SELECT * FROM timers WHERE id = :id")
    suspend fun getTimerById(id: Long): TimerEntity?

    /**
     * Get timer by ID as Flow.
     */
    @Query("SELECT * FROM timers WHERE id = :id")
    fun getTimerByIdFlow(id: Long): Flow<TimerEntity?>

    /**
     * Get currently running timer (only one should run at a time).
     */
    @Query("SELECT * FROM timers WHERE isRunning = 1 LIMIT 1")
    suspend fun getRunningTimer(): TimerEntity?

    /**
     * Insert new timer. Returns the auto-generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: TimerEntity): Long

    /**
     * Update existing timer.
     */
    @Update
    suspend fun update(timer: TimerEntity)

    /**
     * Delete timer.
     */
    @Delete
    suspend fun delete(timer: TimerEntity)

    /**
     * Delete timer by ID.
     */
    @Query("DELETE FROM timers WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Delete all finished timers.
     */
    @Query("DELETE FROM timers WHERE isFinished = 1")
    suspend fun deleteFinishedTimers()

    /**
     * Delete all timers.
     */
    @Query("DELETE FROM timers")
    suspend fun deleteAll()

    /**
     * Update timer state (running/paused).
     */
    @Query("UPDATE timers SET isRunning = :isRunning, isPaused = :isPaused WHERE id = :id")
    suspend fun updateState(id: Long, isRunning: Boolean, isPaused: Boolean)

    /**
     * Update remaining time.
     */
    @Query("UPDATE timers SET remainingMillis = :remainingMillis WHERE id = :id")
    suspend fun updateRemainingTime(id: Long, remainingMillis: Long)

    /**
     * Mark timer as finished.
     */
    @Query("UPDATE timers SET isFinished = 1, isRunning = 0, isPaused = 0 WHERE id = :id")
    suspend fun markAsFinished(id: Long)

    /**
     * Get count of active timers.
     */
    @Query("SELECT COUNT(*) FROM timers WHERE isFinished = 0")
    suspend fun getActiveTimerCount(): Int
}
