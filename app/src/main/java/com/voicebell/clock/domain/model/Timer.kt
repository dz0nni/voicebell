package com.voicebell.clock.domain.model

/**
 * Domain model representing a timer.
 */
data class Timer(
    val id: Long = 0,
    val label: String = "",
    val durationMillis: Long,
    val remainingMillis: Long,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val startTime: Long = 0,
    val pauseTime: Long = 0,
    val endTime: Long = 0,
    val vibrate: Boolean = true,
    val ringtone: String = "default",
    val createdAt: Long = System.currentTimeMillis()
) {

    /**
     * Get current remaining time accounting for running state.
     */
    fun getCurrentRemainingMillis(): Long {
        return when {
            isFinished -> 0
            isRunning && !isPaused -> {
                val elapsed = System.currentTimeMillis() - startTime
                maxOf(0, remainingMillis - elapsed)
            }
            else -> remainingMillis
        }
    }

    /**
     * Get formatted remaining time (MM:SS or HH:MM:SS).
     */
    fun getFormattedTime(): String {
        val remaining = getCurrentRemainingMillis()
        val seconds = (remaining / 1000) % 60
        val minutes = (remaining / 60000) % 60
        val hours = remaining / 3600000

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Get progress percentage (0.0 to 1.0).
     */
    fun getProgress(): Float {
        if (durationMillis == 0L) return 1f
        val elapsed = durationMillis - getCurrentRemainingMillis()
        return (elapsed.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Check if timer needs updating (is running).
     */
    fun needsUpdate(): Boolean {
        return isRunning && !isPaused && !isFinished
    }
}
