package com.voicebell.clock.data.local.database.dao

import androidx.room.*
import com.voicebell.clock.data.local.database.entities.StopwatchStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for stopwatch state operations.
 *
 * Stopwatch state is a singleton (always id = 1).
 */
@Dao
interface StopwatchDao {

    /**
     * Get stopwatch state as Flow.
     */
    @Query("SELECT * FROM stopwatch_state WHERE id = 1")
    fun getStopwatchState(): Flow<StopwatchStateEntity?>

    /**
     * Get stopwatch state (single read).
     */
    @Query("SELECT * FROM stopwatch_state WHERE id = 1")
    suspend fun getStopwatchStateOnce(): StopwatchStateEntity?

    /**
     * Insert or update stopwatch state.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: StopwatchStateEntity)

    /**
     * Update stopwatch state.
     */
    @Update
    suspend fun update(state: StopwatchStateEntity)

    /**
     * Delete stopwatch state (reset).
     */
    @Query("DELETE FROM stopwatch_state WHERE id = 1")
    suspend fun delete()

    /**
     * Reset stopwatch to initial state.
     */
    @Transaction
    suspend fun reset() {
        delete()
        insertOrUpdate(
            StopwatchStateEntity(
                id = 1,
                isRunning = false,
                isPaused = false,
                startTime = 0,
                pausedTime = 0,
                elapsedMillis = 0,
                pausedElapsedMillis = 0,
                lapTimes = emptyList(),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}
