package com.voicebell.clock.domain.repository

import com.voicebell.clock.domain.model.WorldClock
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for world clock operations.
 */
interface WorldClockRepository {

    /**
     * Get all world clocks as Flow.
     */
    fun getAllWorldClocks(): Flow<List<WorldClock>>

    /**
     * Get world clock by ID.
     */
    suspend fun getWorldClockById(id: Long): WorldClock?

    /**
     * Get world clock by timezone ID.
     */
    suspend fun getWorldClockByTimeZone(timeZoneId: String): WorldClock?

    /**
     * Check if timezone already exists.
     */
    suspend fun timeZoneExists(timeZoneId: String): Boolean

    /**
     * Create new world clock. Returns the generated ID.
     */
    suspend fun createWorldClock(worldClock: WorldClock): Long

    /**
     * Update existing world clock.
     */
    suspend fun updateWorldClock(worldClock: WorldClock)

    /**
     * Delete world clock.
     */
    suspend fun deleteWorldClock(id: Long)

    /**
     * Update sort order.
     */
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    /**
     * Get world clock count.
     */
    suspend fun getWorldClockCount(): Int
}
