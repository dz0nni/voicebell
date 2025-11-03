package com.voicebell.clock.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a world clock in the database.
 */
@Entity(tableName = "world_clocks")
data class WorldClockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Location
    val cityName: String,                   // e.g., "New York"
    val countryName: String,                // e.g., "United States"
    val timeZoneId: String,                 // IANA timezone: "America/New_York"

    // Display order
    val sortOrder: Int = 0,

    // Metadata
    val createdAt: Long = System.currentTimeMillis()
)
