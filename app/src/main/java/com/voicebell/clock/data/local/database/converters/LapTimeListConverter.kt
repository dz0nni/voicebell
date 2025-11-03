package com.voicebell.clock.data.local.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Room TypeConverter for converting List<Long> to/from JSON string.
 *
 * Used for storing stopwatch lap times in the database.
 */
class LapTimeListConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLapTimeList(lapTimes: List<Long>): String {
        return json.encodeToString(lapTimes)
    }

    @TypeConverter
    fun toLapTimeList(lapTimesString: String): List<Long> {
        return if (lapTimesString.isEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString(lapTimesString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
