package com.voicebell.clock.data.local.database.dao

import androidx.room.*
import com.voicebell.clock.data.local.database.entities.WorldClockEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for world clock database operations.
 */
@Dao
interface WorldClockDao {

    /**
     * Get all world clocks as Flow.
     * Ordered by user-defined sort order.
     */
    @Query("SELECT * FROM world_clocks ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllWorldClocks(): Flow<List<WorldClockEntity>>

    /**
     * Get world clock by ID.
     */
    @Query("SELECT * FROM world_clocks WHERE id = :id")
    suspend fun getWorldClockById(id: Long): WorldClockEntity?

    /**
     * Get world clock by timezone ID.
     */
    @Query("SELECT * FROM world_clocks WHERE timeZoneId = :timeZoneId")
    suspend fun getWorldClockByTimeZone(timeZoneId: String): WorldClockEntity?

    /**
     * Check if timezone already exists.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM world_clocks WHERE timeZoneId = :timeZoneId)")
    suspend fun timeZoneExists(timeZoneId: String): Boolean

    /**
     * Insert new world clock. Returns the auto-generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(worldClock: WorldClockEntity): Long

    /**
     * Insert multiple world clocks.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(worldClocks: List<WorldClockEntity>)

    /**
     * Update existing world clock.
     */
    @Update
    suspend fun update(worldClock: WorldClockEntity)

    /**
     * Delete world clock.
     */
    @Delete
    suspend fun delete(worldClock: WorldClockEntity)

    /**
     * Delete world clock by ID.
     */
    @Query("DELETE FROM world_clocks WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Delete all world clocks.
     */
    @Query("DELETE FROM world_clocks")
    suspend fun deleteAll()

    /**
     * Update sort order.
     */
    @Query("UPDATE world_clocks SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    /**
     * Get count of world clocks.
     */
    @Query("SELECT COUNT(*) FROM world_clocks")
    suspend fun getWorldClockCount(): Int

    /**
     * Get maximum sort order (for adding new clocks at the end).
     */
    @Query("SELECT MAX(sortOrder) FROM world_clocks")
    suspend fun getMaxSortOrder(): Int?
}
