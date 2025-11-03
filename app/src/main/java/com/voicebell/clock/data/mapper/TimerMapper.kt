package com.voicebell.clock.data.mapper

import com.voicebell.clock.data.local.database.entities.TimerEntity
import com.voicebell.clock.domain.model.Timer

/**
 * Mapper functions to convert between TimerEntity and Timer.
 */

/**
 * Convert TimerEntity to Timer (domain model).
 */
fun TimerEntity.toDomain(): Timer {
    return Timer(
        id = id,
        label = label,
        durationMillis = durationMillis,
        remainingMillis = remainingMillis,
        isRunning = isRunning,
        isPaused = isPaused,
        isFinished = isFinished,
        startTime = startTime,
        pauseTime = pauseTime,
        endTime = endTime,
        vibrate = vibrate,
        ringtone = ringtone,
        createdAt = createdAt
    )
}

/**
 * Convert Timer (domain model) to TimerEntity.
 */
fun Timer.toEntity(): TimerEntity {
    return TimerEntity(
        id = id,
        label = label,
        durationMillis = durationMillis,
        remainingMillis = remainingMillis,
        isRunning = isRunning,
        isPaused = isPaused,
        isFinished = isFinished,
        startTime = startTime,
        pauseTime = pauseTime,
        endTime = endTime,
        vibrate = vibrate,
        ringtone = ringtone,
        createdAt = createdAt
    )
}

/**
 * Convert list of TimerEntity to list of Timer.
 */
fun List<TimerEntity>.toDomain(): List<Timer> {
    return map { it.toDomain() }
}
