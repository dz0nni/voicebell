package com.voicebell.clock.domain.repository

import com.voicebell.clock.domain.model.Timer
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for timer operations.
 */
interface TimerRepository {

    /**
     * Get all timers as Flow.
     */
    fun getAllTimers(): Flow<List<Timer>>

    /**
     * Get all active timers.
     */
    fun getActiveTimers(): Flow<List<Timer>>

    /**
     * Get timer by ID.
     */
    suspend fun getTimerById(id: Long): Timer?

    /**
     * Get timer by ID as Flow.
     */
    fun getTimerByIdFlow(id: Long): Flow<Timer?>

    /**
     * Get currently running timer.
     */
    suspend fun getRunningTimer(): Timer?

    /**
     * Create new timer. Returns the generated ID.
     */
    suspend fun createTimer(timer: Timer): Long

    /**
     * Update existing timer.
     */
    suspend fun updateTimer(timer: Timer)

    /**
     * Delete timer.
     */
    suspend fun deleteTimer(id: Long)

    /**
     * Delete all finished timers.
     */
    suspend fun deleteFinishedTimers()

    /**
     * Update timer state.
     */
    suspend fun updateTimerState(id: Long, isRunning: Boolean, isPaused: Boolean)

    /**
     * Update remaining time.
     */
    suspend fun updateRemainingTime(id: Long, remainingMillis: Long)

    /**
     * Mark timer as finished.
     */
    suspend fun markAsFinished(id: Long)

    /**
     * Get count of active timers.
     */
    suspend fun getActiveTimerCount(): Int
}
