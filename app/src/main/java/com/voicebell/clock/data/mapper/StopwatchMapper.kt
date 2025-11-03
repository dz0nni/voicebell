package com.voicebell.clock.data.mapper

import com.voicebell.clock.data.local.database.entities.StopwatchStateEntity
import com.voicebell.clock.domain.model.Stopwatch

/**
 * Mapper functions to convert between StopwatchStateEntity and Stopwatch.
 */

/**
 * Convert StopwatchStateEntity to Stopwatch (domain model).
 */
fun StopwatchStateEntity.toDomain(): Stopwatch {
    return Stopwatch(
        isRunning = isRunning,
        isPaused = isPaused,
        startTime = startTime,
        pausedTime = pausedTime,
        elapsedMillis = elapsedMillis,
        pausedElapsedMillis = pausedElapsedMillis,
        laps = lapTimes.mapIndexed { index, lapTime ->
            val totalTime = lapTimes.take(index + 1).sum()
            Stopwatch.Lap(
                lapNumber = index + 1,
                lapTime = lapTime,
                totalTime = totalTime
            )
        },
        lastUpdated = lastUpdated
    )
}

/**
 * Convert Stopwatch (domain model) to StopwatchStateEntity.
 */
fun Stopwatch.toEntity(): StopwatchStateEntity {
    return StopwatchStateEntity(
        id = 1, // Always 1 (singleton)
        isRunning = isRunning,
        isPaused = isPaused,
        startTime = startTime,
        pausedTime = pausedTime,
        elapsedMillis = elapsedMillis,
        pausedElapsedMillis = pausedElapsedMillis,
        lapTimes = laps.map { it.lapTime },
        lastUpdated = lastUpdated
    )
}
