package com.voicebell.clock.domain.usecase.voice

import android.content.Context
import android.content.Intent
import android.util.Log
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.AlarmTone
import com.voicebell.clock.domain.model.DayOfWeek
import com.voicebell.clock.domain.usecase.alarm.CreateAlarmUseCase
import com.voicebell.clock.domain.usecase.timer.StartTimerUseCase
import com.voicebell.clock.service.TimerService
import com.voicebell.clock.util.VoiceCommandParser
import com.voicebell.clock.util.VoiceCommandResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for executing parsed voice commands.
 *
 * This bridges the gap between voice recognition and actual actions:
 * - Alarm commands -> Create alarm
 * - Timer commands -> Start timer
 * - Query commands -> Return status
 * - Cancel commands -> Cancel alarm/timer
 *
 * Based on the original architecture plan.
 */
class ExecuteVoiceCommandUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voiceCommandParser: VoiceCommandParser,
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val startTimerUseCase: StartTimerUseCase
) {

    companion object {
        private const val TAG = "ExecuteVoiceCommand"
    }

    /**
     * Execute a voice command from recognized text.
     *
     * @param recognizedText The text recognized by Vosk
     * @return Result of command execution
     */
    suspend operator fun invoke(recognizedText: String): CommandExecutionResult {
        Log.d(TAG, "Executing voice command: $recognizedText")

        // Normalize recognized text (fix common recognition errors)
        val normalizedText = normalizeRecognizedText(recognizedText)
        if (normalizedText != recognizedText) {
            Log.d(TAG, "Normalized to: $normalizedText")
        }

        // Parse the command
        val parseResult = voiceCommandParser.parseCommand(normalizedText)

        // Execute based on command type
        return when (parseResult) {
            is VoiceCommandResult.AlarmCommand -> executeAlarmCommand(parseResult)
            is VoiceCommandResult.TimerCommand -> executeTimerCommand(parseResult)
            is VoiceCommandResult.Unknown -> {
                Log.w(TAG, "Unknown command: ${parseResult.originalText}")
                CommandExecutionResult.Error("I didn't understand that command")
            }
            is VoiceCommandResult.Error -> {
                Log.w(TAG, "Parse error: ${parseResult.message}")
                CommandExecutionResult.Error(parseResult.message)
            }
        }
    }

    /**
     * Execute alarm command.
     */
    private suspend fun executeAlarmCommand(command: VoiceCommandResult.AlarmCommand): CommandExecutionResult {
        return try {
            // Adjust alarm time to next occurrence if it's in the past today
            val adjustedTime = getNextOccurrenceTime(command.time, command.isExplicitTime)
            val adjustedCommand = command.copy(time = adjustedTime)

            val alarm = createAlarmFromCommand(adjustedCommand)

            Log.d(TAG, "Creating alarm for ${adjustedCommand.time} (original: ${command.time}, explicit: ${command.isExplicitTime})")
            createAlarmUseCase(alarm)

            val message = buildAlarmConfirmationMessage(adjustedCommand)
            CommandExecutionResult.Success(message)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating alarm", e)
            CommandExecutionResult.Error("Failed to create alarm")
        }
    }

    /**
     * Get the next occurrence of the given time with intelligent AM/PM inference.
     *
     * For ambiguous hours (1-11 without explicit AM/PM):
     * - If time was explicitly specified with context words (morning, evening, etc.), keep as-is
     * - If the time already passed today, try PM version (add 12 hours)
     * - If PM version is still in future today, use it
     * - Otherwise, keep original (will be scheduled for tomorrow)
     *
     * Examples:
     * - Current: 15:00, Say "four" → 04:00 < 15:00 → Try 16:00 > 15:00 ✓ → Use 16:00
     * - Current: 20:00, Say "seven" → 07:00 < 20:00 → Try 19:00 < 20:00 ✗ → Use 07:00 (tomorrow)
     * - Current: 15:00, Say "seven in the morning" → 07:00 explicit → Keep 07:00 (tomorrow)
     */
    private fun getNextOccurrenceTime(time: LocalTime, isExplicitTime: Boolean): LocalTime {
        val now = LocalTime.now()

        // If time was explicitly specified with context words, don't apply PM inference
        if (isExplicitTime) {
            return time
        }

        // For ambiguous hours (1-11), try PM version if AM version is in the past
        if (time.hour in 1..11 && time.isBefore(now)) {
            val pmTime = time.plusHours(12)
            // If PM version is still in future today, use it
            if (pmTime.isAfter(now)) {
                return pmTime
            }
        }

        return time
    }

    /**
     * Execute timer command.
     */
    private suspend fun executeTimerCommand(command: VoiceCommandResult.TimerCommand): CommandExecutionResult {
        return try {
            val label = command.label ?: "" // No default label

            Log.d(TAG, "Starting timer for ${command.durationMillis}ms")
            val result = startTimerUseCase(
                durationMillis = command.durationMillis,
                label = label
            )

            if (result.isFailure) {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to start timer", error)
                return CommandExecutionResult.Error(error?.message ?: "Failed to start timer")
            }

            // Get the timer ID from the result
            val timerId = result.getOrNull()
            if (timerId == null) {
                Log.e(TAG, "Timer ID is null after creation")
                return CommandExecutionResult.Error("Failed to start timer")
            }

            // Start TimerService to monitor the timer
            Log.d(TAG, "Starting TimerService for timer ID: $timerId")
            val intent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_START
                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
            }
            context.startForegroundService(intent)

            val message = buildTimerConfirmationMessage(command)
            CommandExecutionResult.Success(message)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting timer", e)
            CommandExecutionResult.Error("Failed to start timer")
        }
    }

    /**
     * Create Alarm domain model from parsed command.
     */
    private fun createAlarmFromCommand(command: VoiceCommandResult.AlarmCommand): Alarm {
        return Alarm(
            id = 0, // Auto-generated by database
            time = command.time,
            isEnabled = true,
            label = command.label ?: "", // No default label
            alarmTone = AlarmTone.DEFAULT,
            repeatDays = emptySet(), // One-time alarm by default
            vibrate = true,
            flash = false,
            gradualVolumeIncrease = true,
            volumeLevel = 80,
            snoozeEnabled = true,
            snoozeDuration = 10, // 10 minutes default
            snoozeCount = 0,
            maxSnoozeCount = 3,
            preAlarmCount = 0, // No pre-alarms by default
            preAlarmInterval = 7
        )
    }

    /**
     * Build confirmation message for alarm.
     */
    private fun buildAlarmConfirmationMessage(command: VoiceCommandResult.AlarmCommand): String {
        val time = command.time
        val formattedTime = formatTime(time)
        val label = command.label?.let { " '$it'" } ?: ""

        return "Alarm set for $formattedTime$label"
    }

    /**
     * Build confirmation message for timer.
     */
    private fun buildTimerConfirmationMessage(command: VoiceCommandResult.TimerCommand): String {
        val duration = formatDuration(command.durationMillis)
        val label = command.label?.let { " '$it'" } ?: ""

        return "Timer set for $duration$label"
    }

    /**
     * Format time for display (12-hour format with AM/PM).
     */
    private fun formatTime(time: LocalTime): String {
        val hour = time.hour
        val minute = time.minute

        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        val amPm = if (hour < 12) "AM" else "PM"
        val minuteStr = minute.toString().padStart(2, '0')

        return "$displayHour:$minuteStr $amPm"
    }

    /**
     * Format duration for display.
     */
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> {
                val remainingMinutes = minutes % 60
                if (remainingMinutes > 0) {
                    "$hours hour${if (hours > 1) "s" else ""} and $remainingMinutes minute${if (remainingMinutes > 1) "s" else ""}"
                } else {
                    "$hours hour${if (hours > 1) "s" else ""}"
                }
            }
            minutes > 0 -> {
                val remainingSeconds = seconds % 60
                if (remainingSeconds > 0) {
                    "$minutes minute${if (minutes > 1) "s" else ""} and $remainingSeconds second${if (remainingSeconds > 1) "s" else ""}"
                } else {
                    "$minutes minute${if (minutes > 1) "s" else ""}"
                }
            }
            else -> "$seconds second${if (seconds != 1) "s" else ""}"
        }
    }

    /**
     * Normalize recognized text to fix common speech recognition errors.
     *
     * Common fixes:
     * - "then" → "ten" (e.g., "eight then" → "eight ten" → 08:10)
     * - "won" → "one" (e.g., "alarm won thirty" → "alarm one thirty" → 01:30)
     * - "ate" → "eight" (e.g., "alarm ate thirty" → "alarm eight thirty" → 08:30)
     * - "said" → "set" (e.g., "said alarm" → "set alarm")
     */
    private fun normalizeRecognizedText(text: String): String {
        var normalized = text

        // Replace common misheard words (case-insensitive, word boundaries)
        normalized = normalized.replace(Regex("\\bthen\\b", RegexOption.IGNORE_CASE), "ten")
        normalized = normalized.replace(Regex("\\bwon\\b", RegexOption.IGNORE_CASE), "one")
        normalized = normalized.replace(Regex("\\bate\\b", RegexOption.IGNORE_CASE), "eight")
        normalized = normalized.replace(Regex("\\bsaid\\b", RegexOption.IGNORE_CASE), "set")

        return normalized
    }
}

/**
 * Result of executing a voice command.
 */
sealed class CommandExecutionResult {
    /**
     * Command executed successfully.
     */
    data class Success(val message: String) : CommandExecutionResult()

    /**
     * Command execution failed.
     */
    data class Error(val message: String) : CommandExecutionResult()
}
