package com.voicebell.clock.domain.repository

import com.voicebell.clock.domain.model.Stopwatch
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for stopwatch operations.
 */
interface StopwatchRepository {

    /**
     * Get stopwatch state as Flow.
     */
    fun getStopwatchState(): Flow<Stopwatch?>

    /**
     * Get stopwatch state (single read).
     */
    suspend fun getStopwatchStateOnce(): Stopwatch?

    /**
     * Save stopwatch state.
     */
    suspend fun saveStopwatchState(stopwatch: Stopwatch)

    /**
     * Reset stopwatch to initial state.
     */
    suspend fun resetStopwatch()
}
