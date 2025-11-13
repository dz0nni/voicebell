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
            // Fallback: try to infer command type from content
            else -> inferCommandFromContent(normalizedText, text)
        }
    }

    /**
     * Try to infer command type from content even without explicit keywords.
     * This helps with partial/distorted voice recognition.
     */
    private fun inferCommandFromContent(normalizedText: String, originalText: String): VoiceCommandResult {
        // Check if there's a duration mentioned - likely a timer
        val duration = extractDuration(normalizedText)
        if (duration != null && duration > 0) {
            return VoiceCommandResult.TimerCommand(
                durationMillis = duration,
                label = null
            )
        }

        // Check if there's a time mentioned - likely an alarm
        val time = extractTime(normalizedText)
        if (time != null) {
            return VoiceCommandResult.AlarmCommand(
                time = time,
                label = null
            )
        }

        return VoiceCommandResult.Unknown(originalText)
    }

    /**
     * Checks if the text is an alarm command.
     */
    private fun isAlarmCommand(text: String): Boolean {
        val alarmKeywords = listOf("alarm", "wake me", "wake up", "wake", "me up at", "up at")
        return alarmKeywords.any { text.contains(it) }
    }

    /**
     * Checks if the text is a timer command.
     */
    private fun isTimerCommand(text: String): Boolean {
        val timerKeywords = listOf("timer", "countdown", "count down")
        // Also check for patterns like "for X minutes/seconds"
        val forDurationPattern = """for\s+\d+\s+(?:minute|second|hour)""".toRegex()
        return timerKeywords.any { text.contains(it) } || forDurationPattern.containsMatchIn(text)
    }

    /**
     * Parses an alarm command to extract time.
     */
    private fun parseAlarmCommand(text: String): VoiceCommandResult {
        // Try to extract time from various formats
        val time = extractTime(text)

        return if (time != null) {
            // Check if time was explicitly specified with context words
            val isExplicitTime = hasExplicitTimeContext(text)

            VoiceCommandResult.AlarmCommand(
                time = time,
                label = extractLabel(text),
                isExplicitTime = isExplicitTime
            )
        } else {
            VoiceCommandResult.Error("Could not understand the time. Please try again.")
        }
    }

    /**
     * Check if text contains explicit time context words (AM/PM or time-of-day words).
     */
    private fun hasExplicitTimeContext(text: String): Boolean {
        val timeContextWords = listOf(
            "am", "pm",
            "morning", "evening", "afternoon", "night", "tonight"
        )
        return timeContextWords.any { text.contains(it) }
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
        // Early exit: if text contains duration patterns, it's likely a timer, not an alarm
        val durationPattern = """\d+\s*(?:minute|second|hour)""".toRegex()
        if (durationPattern.containsMatchIn(text)) {
            // This looks like a timer command, not an alarm
            return null
        }

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

        // Pattern 3: Handle compound times like "at seven twenty five"
        // Look for pattern: "at [hour word] [optional minute words]"
        val hourWords = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4,
            "five" to 5, "six" to 6, "seven" to 7, "eight" to 8,
            "nine" to 9, "ten" to 10, "eleven" to 11, "twelve" to 12
        )

        val minuteWords = mapOf(
            "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
            "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
            "eighteen" to 18, "nineteen" to 19, "twenty" to 20, "thirty" to 30,
            "forty" to 40, "fifty" to 50
        )

        val singleDigitWords = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4,
            "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9
        )

        // Pattern 3a: Handle "[minute word(s)] past [hour word]" (e.g., "five past eight", "twenty past seven")
        val pastPattern = """\b(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty)\s+past\s+\b(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve)\b""".toRegex()
        pastPattern.find(text)?.let { match ->
            val minuteWord = match.groupValues[1]
            val hourWord = match.groupValues[2]

            val minute = minuteWords[minuteWord] ?: singleDigitWords[minuteWord] ?: return@let
            val hour = hourWords[hourWord] ?: return@let

            // Check for AM/PM context
            val adjustedHour = when {
                text.contains("pm") || text.contains("evening") || text.contains("night") || text.contains("afternoon") || text.contains("tonight") -> {
                    if (hour == 12) 12 else hour + 12
                }
                text.contains("am") || text.contains("morning") -> {
                    if (hour == 12) 0 else hour
                }
                else -> hour
            }

            if (minute in 0..59 && adjustedHour in 0..23) {
                return LocalTime.of(adjustedHour, minute)
            }
        }

        // Pattern 3b: Handle "[hour word] o [single digit]" (e.g., "eight o four", "seven o nine")
        val oClockPattern = """\b(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve)\s+o\s+(one|two|three|four|five|six|seven|eight|nine)\b""".toRegex()
        oClockPattern.find(text)?.let { match ->
            val hourWord = match.groupValues[1]
            val minuteWord = match.groupValues[2]

            val hour = hourWords[hourWord] ?: return@let
            val minute = singleDigitWords[minuteWord] ?: return@let

            // "o" followed by single digit means "oh-X" (e.g., "eight o four" = 8:04)
            // Check for AM/PM context
            val adjustedHour = when {
                text.contains("pm") || text.contains("evening") || text.contains("night") || text.contains("afternoon") || text.contains("tonight") -> {
                    if (hour == 12) 12 else hour + 12
                }
                text.contains("am") || text.contains("morning") -> {
                    if (hour == 12) 0 else hour
                }
                else -> hour
            }

            if (minute in 0..9 && adjustedHour in 0..23) {
                return LocalTime.of(adjustedHour, minute)
            }
        }

        // Pattern 3c: Handle "[hour word] [single digit]" without "o" (e.g., "eight five", "seven four")
        // This pattern is more ambiguous, so we check it AFTER more specific patterns
        val hourSingleDigitPattern = """\b(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve)\s+(one|two|three|four|five|six|seven|eight|nine)(?:\s|$)""".toRegex()
        hourSingleDigitPattern.find(text)?.let { match ->
            val hourWord = match.groupValues[1]
            val minuteWord = match.groupValues[2]

            val hour = hourWords[hourWord] ?: return@let
            val minute = singleDigitWords[minuteWord] ?: return@let

            // Single digit after hour word means minutes (e.g., "eight five" = 8:05)
            // Check for AM/PM context
            val adjustedHour = when {
                text.contains("pm") || text.contains("evening") || text.contains("night") || text.contains("afternoon") || text.contains("tonight") -> {
                    if (hour == 12) 12 else hour + 12
                }
                text.contains("am") || text.contains("morning") -> {
                    if (hour == 12) 0 else hour
                }
                else -> hour
            }

            if (minute in 0..9 && adjustedHour in 0..23) {
                return LocalTime.of(adjustedHour, minute)
            }
        }

        // Try to find "[at/for] [hour]" pattern first (supports both "at" and "for")
        // Use word boundaries \b to match whole words only (e.g., "seven" but not "seventeen")
        val atOrForHourPattern = """(?:at|for)\s+\b(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve)\b""".toRegex()
        atOrForHourPattern.find(text)?.let { match ->
            val hourWord = match.groupValues[1]
            val hour = hourWords[hourWord] ?: return@let

            // Now look for minutes after the hour
            val remainingText = text.substring(match.range.last + 1)
            var minute = 0

            // Check for compound minutes like "twenty five", "forty five"
            for ((tensWord, tensValue) in minuteWords) {
                if (remainingText.contains(tensWord)) {
                    minute = tensValue
                    // Check if there's a single digit after it
                    for ((digitWord, digitValue) in singleDigitWords) {
                        if (remainingText.contains("$tensWord $digitWord") ||
                            remainingText.contains("$tensWord$digitWord")) {
                            minute += digitValue
                            break
                        }
                    }
                    break
                }
            }

            // If no tens found, check for single digit minutes
            if (minute == 0) {
                for ((digitWord, digitValue) in singleDigitWords) {
                    if (remainingText.trim().startsWith(digitWord)) {
                        minute = digitValue
                        break
                    }
                }
            }

            // Check for AM/PM after minutes
            val adjustedHour = when {
                remainingText.contains("pm") || remainingText.contains("evening") || remainingText.contains("night") || remainingText.contains("afternoon") || remainingText.contains("tonight") -> {
                    if (hour == 12) 12 else hour + 12
                }
                remainingText.contains("am") || remainingText.contains("morning") -> {
                    if (hour == 12) 0 else hour
                }
                else -> hour // Default to as-is if no AM/PM specified
            }

            if (minute in 0..59 && adjustedHour in 0..23) {
                return LocalTime.of(adjustedHour, minute)
            }
        }

        // Pattern 4: Fallback - just look for hour words
        for ((word, number) in hourWords) {
            if (text.contains("$word o'clock") || text.contains(" $word ")) {
                val minute = when {
                    text.contains("thirty") -> 30
                    text.contains("fifteen") || text.contains("quarter") -> 15
                    text.contains("forty five") -> 45
                    else -> 0
                }

                // Determine AM/PM from context
                val hour = when {
                    text.contains("pm") || text.contains("evening") || text.contains("night") || text.contains("afternoon") || text.contains("tonight") -> {
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

        // Word numbers - parse compound numbers like "seventy five"
        val tensWords = mapOf(
            "twenty" to 20, "thirty" to 30, "forty" to 40,
            "fifty" to 50, "sixty" to 60, "seventy" to 70,
            "eighty" to 80, "ninety" to 90
        )

        val onesWords = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9
        )

        val teensWords = mapOf(
            "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
            "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
            "eighteen" to 18, "nineteen" to 19
        )

        // Helper function to extract number before a time unit
        fun extractNumberBeforeUnit(unit: String): Int? {
            // Look for pattern: "[number words] unit"
            // Try compound numbers first (e.g., "seventy five seconds")
            for ((tensWord, tensValue) in tensWords) {
                for ((onesWord, onesValue) in onesWords) {
                    if (text.contains("$tensWord $onesWord $unit") ||
                        text.contains("$tensWord$onesWord $unit")) {
                        return tensValue + onesValue
                    }
                }
                // Just tens (e.g., "seventy seconds")
                if (text.contains("$tensWord $unit")) {
                    return tensValue
                }
            }

            // Try teens (e.g., "fifteen seconds")
            for ((teenWord, teenValue) in teensWords) {
                if (text.contains("$teenWord $unit")) {
                    return teenValue
                }
            }

            // Try ones (e.g., "five seconds")
            for ((onesWord, onesValue) in onesWords) {
                if (text.contains("$onesWord $unit")) {
                    return onesValue
                }
            }

            return null
        }

        // Extract hours in words
        extractNumberBeforeUnit("hour")?.let { hours ->
            totalMillis += hours * 3600000L
        }

        // Extract minutes in words
        extractNumberBeforeUnit("minute")?.let { minutes ->
            totalMillis += minutes * 60000L
        }

        // Extract seconds in words
        extractNumberBeforeUnit("second")?.let { seconds ->
            totalMillis += seconds * 1000L
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
        val label: String?,
        val isExplicitTime: Boolean = false
    ) : VoiceCommandResult()

    data class TimerCommand(
        val durationMillis: Long,
        val label: String?
    ) : VoiceCommandResult()

    data class Unknown(val originalText: String) : VoiceCommandResult()

    data class Error(val message: String) : VoiceCommandResult()
}
