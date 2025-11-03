package com.voicebell.clock.domain.model

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Domain model representing a world clock.
 */
data class WorldClock(
    val id: Long = 0,
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {

    /**
     * Get current time in this timezone.
     */
    fun getCurrentTime(): ZonedDateTime {
        return ZonedDateTime.now(ZoneId.of(timeZoneId))
    }

    /**
     * Get formatted time string (e.g., "14:30").
     */
    fun getFormattedTime(use24Hour: Boolean = false): String {
        val currentTime = getCurrentTime()
        val formatter = if (use24Hour) {
            DateTimeFormatter.ofPattern("HH:mm")
        } else {
            DateTimeFormatter.ofPattern("hh:mm a")
        }
        return currentTime.format(formatter)
    }

    /**
     * Get timezone offset string (e.g., "GMT+3", "GMT-5").
     */
    fun getTimezoneOffset(): String {
        val currentTime = getCurrentTime()
        val offset = currentTime.offset
        val hours = offset.totalSeconds / 3600
        return if (hours >= 0) {
            "GMT+$hours"
        } else {
            "GMT$hours"
        }
    }

    /**
     * Get day difference from local time (-1, 0, +1).
     */
    fun getDayDifference(): Int {
        val localTime = ZonedDateTime.now()
        val worldTime = getCurrentTime()
        return worldTime.dayOfYear - localTime.dayOfYear
    }

    /**
     * Get display name (e.g., "New York, United States").
     */
    fun getDisplayName(): String {
        return "$cityName, $countryName"
    }
}
