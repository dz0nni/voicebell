package com.voicebell.clock.data.repository

import com.voicebell.clock.data.local.database.dao.WorldClockDao
import com.voicebell.clock.data.mapper.toDomain
import com.voicebell.clock.data.mapper.toEntity
import com.voicebell.clock.domain.model.WorldClock
import com.voicebell.clock.domain.repository.WorldClockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of WorldClockRepository.
 */
class WorldClockRepositoryImpl @Inject constructor(
    private val worldClockDao: WorldClockDao
) : WorldClockRepository {

    override fun getAllWorldClocks(): Flow<List<WorldClock>> {
        return worldClockDao.getAllWorldClocks().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getWorldClockById(id: Long): WorldClock? {
        return worldClockDao.getWorldClockById(id)?.toDomain()
    }

    override suspend fun getWorldClockByTimeZone(timeZoneId: String): WorldClock? {
        return worldClockDao.getWorldClockByTimeZone(timeZoneId)?.toDomain()
    }

    override suspend fun timeZoneExists(timeZoneId: String): Boolean {
        return worldClockDao.timeZoneExists(timeZoneId)
    }

    override suspend fun createWorldClock(worldClock: WorldClock): Long {
        // Get next sort order
        val maxSortOrder = worldClockDao.getMaxSortOrder() ?: -1
        val worldClockWithOrder = worldClock.copy(sortOrder = maxSortOrder + 1)
        return worldClockDao.insert(worldClockWithOrder.toEntity())
    }

    override suspend fun updateWorldClock(worldClock: WorldClock) {
        worldClockDao.update(worldClock.toEntity())
    }

    override suspend fun deleteWorldClock(id: Long) {
        worldClockDao.deleteById(id)
    }

    override suspend fun updateSortOrder(id: Long, sortOrder: Int) {
        worldClockDao.updateSortOrder(id, sortOrder)
    }

    override suspend fun getWorldClockCount(): Int {
        return worldClockDao.getWorldClockCount()
    }
}
