package com.voicebell.clock.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.voicebell.clock.data.local.database.converters.LapTimeListConverter
import com.voicebell.clock.data.local.database.dao.AlarmDao
import com.voicebell.clock.data.local.database.dao.SettingsDao
import com.voicebell.clock.data.local.database.dao.StopwatchDao
import com.voicebell.clock.data.local.database.dao.TimerDao
import com.voicebell.clock.data.local.database.dao.WorldClockDao
import com.voicebell.clock.data.local.database.entities.AlarmEntity
import com.voicebell.clock.data.local.database.entities.SettingsEntity
import com.voicebell.clock.data.local.database.entities.StopwatchStateEntity
import com.voicebell.clock.data.local.database.entities.TimerEntity
import com.voicebell.clock.data.local.database.entities.WorldClockEntity

/**
 * VoiceBell Room Database.
 *
 * Stores all app data locally:
 * - Alarms
 * - Timers
 * - World clocks
 * - Stopwatch state
 * - Settings (UI mode, preferences)
 *
 * All data is stored locally in SQLite. No network access.
 */
@Database(
    entities = [
        AlarmEntity::class,
        TimerEntity::class,
        WorldClockEntity::class,
        StopwatchStateEntity::class,
        SettingsEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(LapTimeListConverter::class)
abstract class ClockDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun timerDao(): TimerDao
    abstract fun worldClockDao(): WorldClockDao
    abstract fun stopwatchDao(): StopwatchDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "voicebell_clock.db"
    }
}
