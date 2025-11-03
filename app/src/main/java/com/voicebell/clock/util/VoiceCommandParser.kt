package com.voicebell.clock.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for voice commands to extract alarm/timer information.
 *
 * Supported commands:
 * - Alarms: "set alarm for 7 AM", "alarm at 8:30", "wake me up at 6 o'clock"
 * - Timers: "set timer for 5 minutes", "timer 10 seconds", "countdown 1 hour"
 */
@Singleton
class VoiceCommandParser @Inject constructor() {

    /**
     * Parses a voice command and returns the result.
     */
    fun parseCommand(text: String): VoiceCommandResult {
        val normalizedText = text.lowercase().trim()

        return when {
            isAlarmCommand(normalizedText) -> parseAlarmCommand(normalizedText)
            isTimerCommand(normalizedText) -> parseTimerCommand(normalizedText)
            else -> VoiceCommandResult.Unknown(text)
        }
    }

    /**
     * Checks if the text is an alarm command.
     */
    private fun isAlarmCommand(text: String): Boolean {
        val alarmKeywords = listOf("alarm", "wake me", "wake up")
        return alarmKeywords.any { text.contains(it) }
    }

    /**
     * Checks if the text is a timer command.
     */
    private fun isTimerCommand(text: String): Boolean {
        val timerKeywords = listOf("timer", "countdown", "count down")
        return timerKeywords.any { text.contains(it) }
    }

    /**
     * Parses an alarm command to extract time.
     */
    private fun parseAlarmCommand(text: String): VoiceCommandResult {
        // Try to extract time from various formats
        val time = extractTime(text)

        return if (time != null) {
            VoiceCommandResult.AlarmCommand(
                time = time,
                label = extractLabel(text)
            )
        } else {
            VoiceCommandResult.Error("Could not understand the time. Please try again.")
        }
    }

    /**
     * Parses a timer command to extract duration.
     */
    private fun parseTimerCommand(text: String): VoiceCommandResult {
        val durationMillis = extractDuration(text)

        return if (durationMillis != null && durationMillis > 0) {
            VoiceCommandResult.TimerCommand(
                durationMillis = durationMillis,
                label = extractLabel(text)
            )
        } else {
            VoiceCommandResult.Error("Could not understand the duration. Please try again.")
        }
    }

    /**
     * Extracts time from text in various formats:
     * - "7 AM", "7:30 PM", "19:30"
     * - "seven o'clock", "half past eight"
     * - "eight thirty"
     */
    private fun extractTime(text: String): LocalTime? {
        // Pattern 1: 7 AM, 7:30 PM
        val timeAmPm = """(\d{1,2})(?::(\d{2}))?\s*(am|pm)""".toRegex()
        timeAmPm.find(text)?.let { match ->
            val hour = match.groupValues[1].toInt()
            val minute = match.groupValues[2].takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val amPm = match.groupValues[3]

            val adjustedHour = when {
                amPm == "pm" && hour != 12 -> hour + 12
                amPm == "am" && hour == 12 -> 0
                else -> hour
            }

            return LocalTime.of(adjustedHour, minute)
        }

        // Pattern 2: 19:30, 7:30 (24-hour format)
        val time24 = """(\d{1,2}):(\d{2})""".toRegex()
        time24.find(text)?.let { match ->
            val hour = match.groupValues[1].toInt()
            val minute = match.groupValues[2].toInt()

            if (hour in 0..23 && minute in 0..59) {
                return LocalTime.of(hour, minute)
            }
        }

        // Pattern 3: "seven o'clock", "eight thirty"
        val wordToNumber = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4,
            "five" to 5, "six" to 6, "seven" to 7, "eight" to 8,
            "nine" to 9, "ten" to 10, "eleven" to 11, "twelve" to 12
        )

        for ((word, number) in wordToNumber) {
            if (text.contains(word)) {
                val minute = when {
                    text.contains("thirty") -> 30
                    text.contains("fifteen") || text.contains("quarter") -> 15
                    text.contains("forty five") -> 45
                    else -> 0
                }

                // Determine AM/PM from context
                val hour = when {
                    text.contains("pm") || text.contains("evening") || text.contains("night") -> {
                        if (number == 12) 12 else number + 12
                    }
                    text.contains("am") || text.contains("morning") -> {
                        if (number == 12) 0 else number
                    }
                    else -> number // Default to the number as-is
                }

                if (hour in 0..23) {
                    return LocalTime.of(hour, minute)
                }
            }
        }

        // Pattern 4: Just a number (e.g., "7", "19")
        val singleNumber = """(?:at|for)?\s*(\d{1,2})(?:\s*o'?clock)?""".toRegex()
        singleNumber.find(text)?.let { match ->
            val hour = match.groupValues[1].toInt()

            if (hour in 0..23) {
                return LocalTime.of(hour, 0)
            } else if (hour in 1..12) {
                // Default to PM if afternoon/evening hours, AM otherwise
                val adjustedHour = if (text.contains("pm") || hour >= 7) hour + 12 else hour
                return LocalTime.of(adjustedHour.coerceIn(0, 23), 0)
            }
        }

        return null
    }

    /**
     * Extracts duration from text in milliseconds.
     * Supports: hours, minutes, seconds
     */
    private fun extractDuration(text: String): Long? {
        var totalMillis = 0L

        // Extract hours
        val hoursRegex = """(\d+)\s*(?:hour|hr)s?""".toRegex()
        hoursRegex.find(text)?.let { match ->
            val hours = match.groupValues[1].toLongOrNull() ?: 0
            totalMillis += hours * 3600000
        }

        // Extract minutes
        val minutesRegex = """(\d+)\s*(?:minute|min)s?""".toRegex()
        minutesRegex.find(text)?.let { match ->
            val minutes = match.groupValues[1].toLongOrNull() ?: 0
            totalMillis += minutes * 60000
        }

        // Extract seconds
        val secondsRegex = """(\d+)\s*(?:second|sec)s?""".toRegex()
        secondsRegex.find(text)?.let { match ->
            val seconds = match.groupValues[1].toLongOrNull() ?: 0
            totalMillis += seconds * 1000
        }

        // Word numbers
        val wordDurations = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
            "fifteen" to 15, "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50
        )

        for ((word, number) in wordDurations) {
            when {
                text.contains("$word hour") -> totalMillis += number * 3600000L
                text.contains("$word minute") -> totalMillis += number * 60000L
                text.contains("$word second") -> totalMillis += number * 1000L
            }
        }

        return if (totalMillis > 0) totalMillis else null
    }

    /**
     * Extracts label from the command (optional).
     */
    private fun extractLabel(text: String): String? {
        // Look for patterns like "called X", "named X", "for X"
        val labelPatterns = listOf(
            """called\s+(.+)""".toRegex(),
            """named\s+(.+)""".toRegex(),
            """label\s+(.+)""".toRegex()
        )

        for (pattern in labelPatterns) {
            pattern.find(text)?.let { match ->
                return match.groupValues[1].trim()
            }
        }

        return null
    }
}

/**
 * Result of parsing a voice command.
 */
sealed class VoiceCommandResult {
    data class AlarmCommand(
        val time: LocalTime,
        val label: String?
    ) : VoiceCommandResult()

    data class TimerCommand(
        val durationMillis: Long,
        val label: String?
    ) : VoiceCommandResult()

    data class Unknown(val originalText: String) : VoiceCommandResult()

    data class Error(val message: String) : VoiceCommandResult()
}
