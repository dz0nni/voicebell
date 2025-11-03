package com.voicebell.clock.domain.model

/**
 * Domain model representing stopwatch state.
 */
data class Stopwatch(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val startTime: Long = 0,
    val pausedTime: Long = 0,
    val elapsedMillis: Long = 0,
    val pausedElapsedMillis: Long = 0,
    val laps: List<Lap> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) {

    /**
     * Get current elapsed time in milliseconds.
     */
    fun getCurrentElapsedMillis(): Long {
        return when {
            isRunning && !isPaused -> {
                pausedElapsedMillis + (System.currentTimeMillis() - startTime)
            }
            isPaused -> pausedElapsedMillis
            else -> elapsedMillis
        }
    }

    /**
     * Get formatted elapsed time (MM:SS.mmm or HH:MM:SS.mmm).
     */
    fun getFormattedTime(): String {
        val elapsed = getCurrentElapsedMillis()
        val millis = elapsed % 1000
        val seconds = (elapsed / 1000) % 60
        val minutes = (elapsed / 60000) % 60
        val hours = elapsed / 3600000

        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        } else {
            String.format("%02d:%02d.%03d", minutes, seconds, millis)
        }
    }

    /**
     * Get current lap time (time since last lap).
     */
    fun getCurrentLapTime(): Long {
        val totalElapsed = getCurrentElapsedMillis()
        val previousLapsTime = laps.sumOf { it.lapTime }
        return totalElapsed - previousLapsTime
    }

    /**
     * Get formatted current lap time.
     */
    fun getFormattedCurrentLapTime(): String {
        val lapTime = getCurrentLapTime()
        val millis = lapTime % 1000
        val seconds = (lapTime / 1000) % 60
        val minutes = (lapTime / 60000) % 60
        val hours = lapTime / 3600000

        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        } else {
            String.format("%02d:%02d.%03d", minutes, seconds, millis)
        }
    }

    /**
     * Check if stopwatch is active (running or paused).
     */
    fun isActive(): Boolean {
        return isRunning || isPaused || elapsedMillis > 0
    }

    /**
     * Represents a single lap.
     */
    data class Lap(
        val lapNumber: Int,
        val lapTime: Long,
        val totalTime: Long
    ) {
        fun getFormattedLapTime(): String {
            val millis = lapTime % 1000
            val seconds = (lapTime / 1000) % 60
            val minutes = (lapTime / 60000) % 60
            val hours = lapTime / 3600000

            return if (hours > 0) {
                String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
            } else {
                String.format("%02d:%02d.%03d", minutes, seconds, millis)
            }
        }

        fun getFormattedTotalTime(): String {
            val millis = totalTime % 1000
            val seconds = (totalTime / 1000) % 60
            val minutes = (totalTime / 60000) % 60
            val hours = totalTime / 3600000

            return if (hours > 0) {
                String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
            } else {
                String.format("%02d:%02d.%03d", minutes, seconds, millis)
            }
        }
    }
}
