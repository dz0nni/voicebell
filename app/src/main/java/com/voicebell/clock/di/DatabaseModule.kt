package com.voicebell.clock.di

import android.content.Context
import androidx.room.Room
import com.voicebell.clock.data.local.database.ClockDatabase
import com.voicebell.clock.data.local.database.dao.AlarmDao
import com.voicebell.clock.data.local.database.dao.SettingsDao
import com.voicebell.clock.data.local.database.dao.StopwatchDao
import com.voicebell.clock.data.local.database.dao.TimerDao
import com.voicebell.clock.data.local.database.dao.WorldClockDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Room database and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideClockDatabase(
        @ApplicationContext context: Context
    ): ClockDatabase {
        return Room.databaseBuilder(
            context,
            ClockDatabase::class.java,
            ClockDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // TODO: Replace with proper migrations in production
            .build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(database: ClockDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    @Singleton
    fun provideTimerDao(database: ClockDatabase): TimerDao {
        return database.timerDao()
    }

    @Provides
    @Singleton
    fun provideWorldClockDao(database: ClockDatabase): WorldClockDao {
        return database.worldClockDao()
    }

    @Provides
    @Singleton
    fun provideStopwatchDao(database: ClockDatabase): StopwatchDao {
        return database.stopwatchDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDao(database: ClockDatabase): SettingsDao {
        return database.settingsDao()
    }
}
