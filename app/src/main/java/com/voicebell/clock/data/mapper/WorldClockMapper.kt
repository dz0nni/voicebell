package com.voicebell.clock.data.mapper

import com.voicebell.clock.data.local.database.entities.WorldClockEntity
import com.voicebell.clock.domain.model.WorldClock

/**
 * Mapper functions to convert between WorldClockEntity and WorldClock.
 */

/**
 * Convert WorldClockEntity to WorldClock (domain model).
 */
fun WorldClockEntity.toDomain(): WorldClock {
    return WorldClock(
        id = id,
        cityName = cityName,
        countryName = countryName,
        timeZoneId = timeZoneId,
        sortOrder = sortOrder,
        createdAt = createdAt
    )
}

/**
 * Convert WorldClock (domain model) to WorldClockEntity.
 */
fun WorldClock.toEntity(): WorldClockEntity {
    return WorldClockEntity(
        id = id,
        cityName = cityName,
        countryName = countryName,
        timeZoneId = timeZoneId,
        sortOrder = sortOrder,
        createdAt = createdAt
    )
}

/**
 * Convert list of WorldClockEntity to list of WorldClock.
 */
fun List<WorldClockEntity>.toDomain(): List<WorldClock> {
    return map { it.toDomain() }
}
