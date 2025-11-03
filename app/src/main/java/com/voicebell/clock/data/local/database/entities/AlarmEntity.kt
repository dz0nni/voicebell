package com.voicebell.clock.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an alarm in the database.
 *
 * All alarm data is stored locally in SQLite database via Room.
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Time
    val hour: Int,                          // 0-23
    val minute: Int,                        // 0-59

    // Status
    val isEnabled: Boolean = true,

    // Basic settings
    val label: String = "",
    val alarmTone: String = "default",      // Tone identifier

    // Repeat settings
    val repeatDays: String = "",            // Comma-separated: "1,2,3,4,5" for Mon-Fri

    // Volume and alerts
    val vibrate: Boolean = true,
    val flash: Boolean = false,
    val gradualVolumeIncrease: Boolean = true,
    val volumeLevel: Int = 80,              // 0-100 percentage

    // Snooze settings
    val snoozeEnabled: Boolean = true,
    val snoozeDuration: Int = 10,           // minutes
    val snoozeCount: Int = 0,               // Current snooze count
    val maxSnoozeCount: Int = 3,            // Maximum snoozes allowed

    // Pre-alarm settings
    val preAlarmCount: Int = 0,             // 0-10
    val preAlarmInterval: Int = 7,          // minutes between pre-alarms

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val nextTriggerTime: Long = 0           // Cached next trigger timestamp
)
