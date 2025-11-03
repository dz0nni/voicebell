package com.voicebell.clock.di

import com.voicebell.clock.data.repository.AlarmRepositoryImpl
import com.voicebell.clock.data.repository.StopwatchRepositoryImpl
import com.voicebell.clock.data.repository.TimerRepositoryImpl
import com.voicebell.clock.data.repository.WorldClockRepositoryImpl
import com.voicebell.clock.domain.repository.AlarmRepository
import com.voicebell.clock.domain.repository.StopwatchRepository
import com.voicebell.clock.domain.repository.TimerRepository
import com.voicebell.clock.domain.repository.WorldClockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        alarmRepositoryImpl: AlarmRepositoryImpl
    ): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindTimerRepository(
        timerRepositoryImpl: TimerRepositoryImpl
    ): TimerRepository

    @Binds
    @Singleton
    abstract fun bindWorldClockRepository(
        worldClockRepositoryImpl: WorldClockRepositoryImpl
    ): WorldClockRepository

    @Binds
    @Singleton
    abstract fun bindStopwatchRepository(
        stopwatchRepositoryImpl: StopwatchRepositoryImpl
    ): StopwatchRepository
}
