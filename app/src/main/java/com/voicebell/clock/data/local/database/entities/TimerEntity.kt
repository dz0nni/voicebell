package com.voicebell.clock.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a timer in the database.
 */
@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Timer configuration
    val label: String = "",
    val durationMillis: Long,               // Total duration
    val remainingMillis: Long,              // Remaining time

    // State
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,

    // Timestamps
    val startTime: Long = 0,                // When timer was started
    val pauseTime: Long = 0,                // When timer was paused
    val endTime: Long = 0,                  // Expected completion time

    // Alert settings
    val vibrate: Boolean = true,
    val ringtone: String = "default",

    // Metadata
    val createdAt: Long = System.currentTimeMillis()
)
