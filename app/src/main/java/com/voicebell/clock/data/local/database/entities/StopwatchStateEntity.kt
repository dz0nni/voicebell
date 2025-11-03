package com.voicebell.clock.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.voicebell.clock.data.local.database.converters.LapTimeListConverter

/**
 * Room entity for persisting stopwatch state.
 *
 * Only one stopwatch state exists at a time (id = 1).
 */
@Entity(tableName = "stopwatch_state")
data class StopwatchStateEntity(
    @PrimaryKey
    val id: Int = 1,                        // Always 1 (singleton)

    // State
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,

    // Timing
    val startTime: Long = 0,                // Timestamp when started
    val pausedTime: Long = 0,               // Timestamp when paused
    val elapsedMillis: Long = 0,            // Total elapsed time
    val pausedElapsedMillis: Long = 0,      // Elapsed time before current pause

    // Lap times (stored as JSON array)
    val lapTimes: List<Long> = emptyList(), // List of lap durations in milliseconds

    // Metadata
    val lastUpdated: Long = System.currentTimeMillis()
)
