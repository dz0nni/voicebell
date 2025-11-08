package com.voicebell.clock.util

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Comprehensive unit tests for VoiceCommandParser.
 *
 * Tests various voice command formats and edge cases for:
 * - Alarm commands (time parsing)
 * - Timer commands (duration parsing)
 * - Error handling
 * - Unknown commands
 */
class VoiceCommandParserTest {

    private lateinit var parser: VoiceCommandParser

    @Before
    fun setup() {
        parser = VoiceCommandParser()
    }

    // ============================================
    // ALARM COMMAND TESTS - AM/PM FORMAT
    // ============================================

    @Test
    fun `parse alarm command with AM time`() {
        // Given
        val command = "set alarm for 7 AM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(7, 0))
    }

    @Test
    fun `parse alarm command with PM time`() {
        // Given
        val command = "set alarm for 8 PM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(20, 0))
    }

    @Test
    fun `parse alarm command with AM time and minutes`() {
        // Given
        val command = "alarm at 6:30 AM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(6, 30))
    }

    @Test
    fun `parse alarm command with PM time and minutes`() {
        // Given
        val command = "wake me up at 9:45 PM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(21, 45))
    }

    @Test
    fun `parse alarm command with 12 AM midnight`() {
        // Given
        val command = "set alarm for 12 AM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(0, 0))
    }

    @Test
    fun `parse alarm command with 12 PM noon`() {
        // Given
        val command = "alarm at 12 PM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(12, 0))
    }

    // ============================================
    // ALARM COMMAND TESTS - 24-HOUR FORMAT
    // ============================================

    @Test
    fun `parse alarm command with 24-hour format`() {
        // Given
        val command = "set alarm for 19:30"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(19, 30))
    }

    @Test
    fun `parse alarm command with 24-hour format morning`() {
        // Given
        val command = "alarm at 08:15"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(8, 15))
    }

    // ============================================
    // ALARM COMMAND TESTS - WORD NUMBERS
    // ============================================

    @Test
    fun `parse alarm command with word number`() {
        // Given
        val command = "set alarm for seven o'clock"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time.hour).isEqualTo(7)
    }

    @Test
    fun `parse alarm command with word number and thirty`() {
        // Given
        val command = "alarm at eight thirty"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(8, 30))
    }

    @Test
    fun `parse alarm command with word number PM`() {
        // Given
        val command = "wake me up at nine PM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(21, 0))
    }

    // ============================================
    // TIMER COMMAND TESTS - MINUTES
    // ============================================

    @Test
    fun `parse timer command with minutes`() {
        // Given
        val command = "set timer for 5 minutes"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(5 * 60 * 1000L)
    }

    @Test
    fun `parse timer command with single minute`() {
        // Given
        val command = "timer for 1 minute"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(60 * 1000L)
    }

    @Test
    fun `parse timer command with minutes abbreviated`() {
        // Given
        val command = "timer 10 min"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(10 * 60 * 1000L)
    }

    // ============================================
    // TIMER COMMAND TESTS - SECONDS
    // ============================================

    @Test
    fun `parse timer command with seconds`() {
        // Given
        val command = "countdown 30 seconds"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(30 * 1000L)
    }

    @Test
    fun `parse timer command with seconds abbreviated`() {
        // Given
        val command = "timer 45 sec"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(45 * 1000L)
    }

    // ============================================
    // TIMER COMMAND TESTS - HOURS
    // ============================================

    @Test
    fun `parse timer command with hours`() {
        // Given
        val command = "set timer for 2 hours"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(2 * 60 * 60 * 1000L)
    }

    @Test
    fun `parse timer command with hour abbreviated`() {
        // Given
        val command = "timer 1 hr"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(60 * 60 * 1000L)
    }

    // ============================================
    // TIMER COMMAND TESTS - COMBINED DURATIONS
    // ============================================

    @Test
    fun `parse timer command with hours and minutes`() {
        // Given
        val command = "set timer for 1 hour 30 minutes"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo((60 + 30) * 60 * 1000L)
    }

    @Test
    fun `parse timer command with minutes and seconds`() {
        // Given
        val command = "timer 5 minutes 30 seconds"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo((5 * 60 + 30) * 1000L)
    }

    @Test
    fun `parse timer command with all units`() {
        // Given
        val command = "set timer for 1 hour 15 minutes 30 seconds"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo((60 * 60 + 15 * 60 + 30) * 1000L)
    }

    // ============================================
    // TIMER COMMAND TESTS - WORD NUMBERS
    // ============================================

    @Test
    fun `parse timer command with word number minutes`() {
        // Given
        val command = "set timer for five minutes"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(5 * 60 * 1000L)
    }

    @Test
    fun `parse timer command with word number seconds`() {
        // Given
        val command = "countdown ten seconds"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(10 * 1000L)
    }

    // ============================================
    // LABEL EXTRACTION TESTS
    // ============================================

    @Test
    fun `parse alarm command with label using called`() {
        // Given
        val command = "set alarm for 7 AM called morning alarm"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.label).isEqualTo("morning alarm")
    }

    @Test
    fun `parse timer command with label using named`() {
        // Given
        val command = "set timer for 5 minutes named workout"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.label).isEqualTo("workout")
    }

    // ============================================
    // ERROR HANDLING TESTS
    // ============================================

    @Test
    fun `parse alarm command with invalid time returns error`() {
        // Given
        val command = "set alarm for tomorrow"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.Error::class.java)
        val error = result as VoiceCommandResult.Error
        assertThat(error.message).contains("Could not understand the time")
    }

    @Test
    fun `parse timer command with no duration returns error`() {
        // Given
        val command = "set timer"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.Error::class.java)
    }

    @Test
    fun `parse timer command with zero duration returns error`() {
        // Given
        val command = "set timer for 0 minutes"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.Error::class.java)
    }

    // ============================================
    // UNKNOWN COMMAND TESTS
    // ============================================

    @Test
    fun `parse unknown command returns Unknown result`() {
        // Given
        val command = "what time is it"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.Unknown::class.java)
        val unknown = result as VoiceCommandResult.Unknown
        assertThat(unknown.originalText).isEqualTo(command)
    }

    @Test
    fun `parse random text returns Unknown result`() {
        // Given
        val command = "hello world"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.Unknown::class.java)
    }

    // ============================================
    // CASE SENSITIVITY TESTS
    // ============================================

    @Test
    fun `parse command is case insensitive for uppercase`() {
        // Given
        val command = "SET ALARM FOR 7 AM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(7, 0))
    }

    @Test
    fun `parse command is case insensitive for mixed case`() {
        // Given
        val command = "SeT tImEr FoR 5 MiNuTeS"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
    }

    // ============================================
    // WHITESPACE HANDLING TESTS
    // ============================================

    @Test
    fun `parse command handles extra whitespace`() {
        // Given
        val command = "  set   alarm   for   7   AM  "

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(7, 0))
    }

    // ============================================
    // NATURAL LANGUAGE VARIATIONS
    // ============================================

    @Test
    fun `parse wake me up variation`() {
        // Given
        val command = "wake me up at 6:30 AM"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
    }

    @Test
    fun `parse countdown variation for timer`() {
        // Given
        val command = "count down 2 minutes"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(2 * 60 * 1000L)
    }

    // ============================================
    // EDGE CASES
    // ============================================

    @Test
    fun `parse empty string returns Unknown`() {
        // Given
        val command = ""

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.Unknown::class.java)
    }

    @Test
    fun `parse only whitespace returns Unknown`() {
        // Given
        val command = "   "

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.Unknown::class.java)
    }

    @Test
    fun `parse alarm with hour 0 is midnight`() {
        // Given
        val command = "alarm at 0:30"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.AlarmCommand::class.java)
        val alarmCommand = result as VoiceCommandResult.AlarmCommand
        assertThat(alarmCommand.time).isEqualTo(LocalTime.of(0, 30))
    }

    @Test
    fun `parse timer with large duration works`() {
        // Given
        val command = "timer for 5 hours"

        // When
        val result = parser.parseCommand(command)

        // Then
        assertThat(result).isInstanceOf(VoiceCommandResult.TimerCommand::class.java)
        val timerCommand = result as VoiceCommandResult.TimerCommand
        assertThat(timerCommand.durationMillis).isEqualTo(5 * 60 * 60 * 1000L)
    }

    // ============================================
    // COMPREHENSIVE 100+ COMMAND VARIATIONS TEST
    // ============================================

    @Test
    fun `comprehensive test of 100 different command variations`() {
        val testCases = listOf(
            // ===== ALARM COMMANDS - NUMERIC TIMES WITH AM/PM =====
            TestCase("set alarm for 7 AM", "Alarm", expectedTime = LocalTime.of(7, 0)),
            TestCase("alarm at 8 AM", "Alarm", expectedTime = LocalTime.of(8, 0)),
            TestCase("wake me up at 9 AM", "Alarm", expectedTime = LocalTime.of(9, 0)),
            TestCase("set alarm for 10 AM", "Alarm", expectedTime = LocalTime.of(10, 0)),
            TestCase("alarm at 11 AM", "Alarm", expectedTime = LocalTime.of(11, 0)),
            TestCase("wake me at 6 PM", "Alarm", expectedTime = LocalTime.of(18, 0)),
            TestCase("set alarm for 7 PM", "Alarm", expectedTime = LocalTime.of(19, 0)),
            TestCase("alarm at 8 PM", "Alarm", expectedTime = LocalTime.of(20, 0)),
            TestCase("wake me up at 9 PM", "Alarm", expectedTime = LocalTime.of(21, 0)),
            TestCase("set alarm for 10 PM", "Alarm", expectedTime = LocalTime.of(22, 0)),

            // ===== ALARM COMMANDS - NUMERIC TIMES WITH MINUTES =====
            TestCase("set alarm for 7:30 AM", "Alarm", expectedTime = LocalTime.of(7, 30)),
            TestCase("alarm at 8:15 AM", "Alarm", expectedTime = LocalTime.of(8, 15)),
            TestCase("wake me up at 6:45 AM", "Alarm", expectedTime = LocalTime.of(6, 45)),
            TestCase("set alarm for 9:30 PM", "Alarm", expectedTime = LocalTime.of(21, 30)),
            TestCase("alarm at 10:15 PM", "Alarm", expectedTime = LocalTime.of(22, 15)),
            TestCase("wake me at 11:45 PM", "Alarm", expectedTime = LocalTime.of(23, 45)),
            TestCase("set alarm for 5:20 AM", "Alarm", expectedTime = LocalTime.of(5, 20)),
            TestCase("alarm at 6:10 AM", "Alarm", expectedTime = LocalTime.of(6, 10)),
            TestCase("wake me up at 7:55 AM", "Alarm", expectedTime = LocalTime.of(7, 55)),
            TestCase("set alarm for 8:05 PM", "Alarm", expectedTime = LocalTime.of(20, 5)),

            // ===== ALARM COMMANDS - WORD NUMBERS =====
            TestCase("set alarm for seven o'clock", "Alarm", expectedTime = LocalTime.of(7, 0)),
            TestCase("alarm at eight o'clock", "Alarm", expectedTime = LocalTime.of(8, 0)),
            TestCase("wake me at nine o'clock", "Alarm", expectedTime = LocalTime.of(9, 0)),
            TestCase("set alarm for ten o'clock", "Alarm", expectedTime = LocalTime.of(10, 0)),
            TestCase("alarm at eleven o'clock", "Alarm", expectedTime = LocalTime.of(11, 0)),
            TestCase("wake me up at twelve o'clock", "Alarm", expectedTime = LocalTime.of(12, 0)),
            TestCase("set alarm for six in the morning", "Alarm", expectedTime = LocalTime.of(6, 0)),
            TestCase("alarm at seven in the morning", "Alarm", expectedTime = LocalTime.of(7, 0)),
            TestCase("wake me at eight in the morning", "Alarm", expectedTime = LocalTime.of(8, 0)),
            TestCase("set alarm for nine in the evening", "Alarm", expectedTime = LocalTime.of(21, 0)),

            // ===== ALARM COMMANDS - WORD NUMBERS WITH MINUTES =====
            TestCase("alarm at seven thirty", "Alarm", expectedTime = LocalTime.of(7, 30)),
            TestCase("wake me at eight thirty", "Alarm", expectedTime = LocalTime.of(8, 30)),
            TestCase("set alarm for nine thirty", "Alarm", expectedTime = LocalTime.of(9, 30)),
            TestCase("alarm at ten thirty", "Alarm", expectedTime = LocalTime.of(10, 30)),
            TestCase("wake me up at seven fifteen", "Alarm", expectedTime = LocalTime.of(7, 15)),
            TestCase("set alarm for eight fifteen", "Alarm", expectedTime = LocalTime.of(8, 15)),
            TestCase("alarm at nine fifteen", "Alarm", expectedTime = LocalTime.of(9, 15)),
            TestCase("wake me at seven forty five", "Alarm", expectedTime = LocalTime.of(7, 45)),
            TestCase("set alarm for eight forty five", "Alarm", expectedTime = LocalTime.of(8, 45)),
            TestCase("alarm at nine forty five", "Alarm", expectedTime = LocalTime.of(9, 45)),

            // ===== ALARM COMMANDS - COMPOUND WORD TIMES (DIFFICULT) =====
            TestCase("alarm at seven twenty five", "Alarm", expectedTime = LocalTime.of(7, 25)),
            TestCase("wake me at eight twenty five", "Alarm", expectedTime = LocalTime.of(8, 25)),
            TestCase("set alarm for nine twenty five", "Alarm", expectedTime = LocalTime.of(9, 25)),
            TestCase("alarm at seven ten", "Alarm", expectedTime = LocalTime.of(7, 10)),
            TestCase("wake me at eight ten", "Alarm", expectedTime = LocalTime.of(8, 10)),
            TestCase("set alarm for nine ten", "Alarm", expectedTime = LocalTime.of(9, 10)),
            TestCase("alarm at ten fifteen PM", "Alarm", expectedTime = LocalTime.of(22, 15)),
            TestCase("wake me at seven ten AM", "Alarm", expectedTime = LocalTime.of(7, 10)),
            TestCase("set alarm for nine twenty PM", "Alarm", expectedTime = LocalTime.of(21, 20)),
            TestCase("alarm at eight fifty five", "Alarm", expectedTime = LocalTime.of(8, 55)),

            // ===== TIMER COMMANDS - MINUTES (NUMERIC) =====
            TestCase("set timer for 5 minutes", "Timer", expectedDuration = 5 * 60 * 1000L),
            TestCase("timer for 10 minutes", "Timer", expectedDuration = 10 * 60 * 1000L),
            TestCase("set timer for 15 minutes", "Timer", expectedDuration = 15 * 60 * 1000L),
            TestCase("timer for 20 minutes", "Timer", expectedDuration = 20 * 60 * 1000L),
            TestCase("set timer for 25 minutes", "Timer", expectedDuration = 25 * 60 * 1000L),
            TestCase("timer for 30 minutes", "Timer", expectedDuration = 30 * 60 * 1000L),
            TestCase("set timer for 45 minutes", "Timer", expectedDuration = 45 * 60 * 1000L),
            TestCase("timer for 60 minutes", "Timer", expectedDuration = 60 * 60 * 1000L),
            TestCase("set timer for 90 minutes", "Timer", expectedDuration = 90 * 60 * 1000L),
            TestCase("timer for 120 minutes", "Timer", expectedDuration = 120 * 60 * 1000L),

            // ===== TIMER COMMANDS - MINUTES (WORD NUMBERS) =====
            TestCase("set timer for five minutes", "Timer", expectedDuration = 5 * 60 * 1000L),
            TestCase("timer for ten minutes", "Timer", expectedDuration = 10 * 60 * 1000L),
            TestCase("set timer for fifteen minutes", "Timer", expectedDuration = 15 * 60 * 1000L),
            TestCase("timer for twenty minutes", "Timer", expectedDuration = 20 * 60 * 1000L),
            TestCase("set timer for thirty minutes", "Timer", expectedDuration = 30 * 60 * 1000L),
            TestCase("timer for forty minutes", "Timer", expectedDuration = 40 * 60 * 1000L),
            TestCase("set timer for fifty minutes", "Timer", expectedDuration = 50 * 60 * 1000L),

            // ===== TIMER COMMANDS - WITHOUT "SET TIMER FOR" PREFIX =====
            TestCase("for 5 minutes", "Timer", expectedDuration = 5 * 60 * 1000L),
            TestCase("for 10 minutes", "Timer", expectedDuration = 10 * 60 * 1000L),
            TestCase("for 15 minutes", "Timer", expectedDuration = 15 * 60 * 1000L),
            TestCase("for 20 minutes", "Timer", expectedDuration = 20 * 60 * 1000L),
            TestCase("for 2 minutes", "Timer", expectedDuration = 2 * 60 * 1000L),
            TestCase("9 minutes", "Timer", expectedDuration = 9 * 60 * 1000L),
            TestCase("17 minutes", "Timer", expectedDuration = 17 * 60 * 1000L),

            // ===== TIMER COMMANDS - SECONDS =====
            TestCase("set timer for 30 seconds", "Timer", expectedDuration = 30 * 1000L),
            TestCase("timer for 45 seconds", "Timer", expectedDuration = 45 * 1000L),
            TestCase("countdown 60 seconds", "Timer", expectedDuration = 60 * 1000L),
            TestCase("set timer for 90 seconds", "Timer", expectedDuration = 90 * 1000L),
            TestCase("timer for 120 seconds", "Timer", expectedDuration = 120 * 1000L),

            // ===== TIMER COMMANDS - HOURS =====
            TestCase("set timer for 1 hour", "Timer", expectedDuration = 60 * 60 * 1000L),
            TestCase("timer for 2 hours", "Timer", expectedDuration = 2 * 60 * 60 * 1000L),
            TestCase("set timer for 3 hours", "Timer", expectedDuration = 3 * 60 * 60 * 1000L),

            // ===== MIXED COMMANDS - ABBREVIATED =====
            TestCase("timer 5 min", "Timer", expectedDuration = 5 * 60 * 1000L),
            TestCase("timer 10 min", "Timer", expectedDuration = 10 * 60 * 1000L),
            TestCase("timer 30 sec", "Timer", expectedDuration = 30 * 1000L),
            TestCase("timer 1 hr", "Timer", expectedDuration = 60 * 60 * 1000L),

            // ===== NATURAL VARIATIONS =====
            TestCase("wake me up at seven thirty in the morning", "Alarm", expectedTime = LocalTime.of(7, 30)),
            TestCase("wake me up at nine thirty in the evening", "Alarm", expectedTime = LocalTime.of(21, 30)),
            TestCase("alarm for seven AM", "Alarm", expectedTime = LocalTime.of(7, 0)),
            TestCase("alarm for nine PM", "Alarm", expectedTime = LocalTime.of(21, 0)),
            TestCase("countdown 5 minutes", "Timer", expectedDuration = 5 * 60 * 1000L),
            TestCase("count down 10 minutes", "Timer", expectedDuration = 10 * 60 * 1000L),

            // ===== 24-HOUR FORMAT =====
            TestCase("set alarm for 19:30", "Alarm", expectedTime = LocalTime.of(19, 30)),
            TestCase("alarm at 20:15", "Alarm", expectedTime = LocalTime.of(20, 15)),
            TestCase("wake me at 06:45", "Alarm", expectedTime = LocalTime.of(6, 45)),
            TestCase("set alarm for 14:00", "Alarm", expectedTime = LocalTime.of(14, 0)),
        )

        // Run all test cases and collect results
        val results = mutableListOf<TestResult>()
        var successCount = 0
        var failCount = 0

        for ((index, testCase) in testCases.withIndex()) {
            val result = parser.parseCommand(testCase.command)
            val success = when {
                testCase.expectedType == "Alarm" && result is VoiceCommandResult.AlarmCommand -> {
                    if (testCase.expectedTime != null) {
                        result.time == testCase.expectedTime
                    } else true
                }
                testCase.expectedType == "Timer" && result is VoiceCommandResult.TimerCommand -> {
                    if (testCase.expectedDuration != null) {
                        result.durationMillis == testCase.expectedDuration
                    } else true
                }
                else -> false
            }

            if (success) {
                successCount++
            } else {
                failCount++
                results.add(TestResult(
                    index = index + 1,
                    command = testCase.command,
                    expectedType = testCase.expectedType,
                    actualResult = result,
                    success = false
                ))
            }
        }

        // Print summary
        println("\n" + "=".repeat(80))
        println("COMPREHENSIVE PARSER TEST RESULTS")
        println("=".repeat(80))
        println("Total tests: ${testCases.size}")
        println("✅ Successful: $successCount (${successCount * 100 / testCases.size}%)")
        println("❌ Failed: $failCount (${failCount * 100 / testCases.size}%)")
        println("=".repeat(80))

        // Print failures
        if (failCount > 0) {
            println("\nFAILED TESTS:")
            println("-".repeat(80))
            for (result in results) {
                println("${result.index}. \"${result.command}\"")
                println("   Expected: ${result.expectedType}")
                println("   Got: ${result.actualResult::class.simpleName}")
                if (result.actualResult is VoiceCommandResult.AlarmCommand) {
                    println("   Time: ${result.actualResult.time}")
                } else if (result.actualResult is VoiceCommandResult.TimerCommand) {
                    println("   Duration: ${result.actualResult.durationMillis / 60000} minutes")
                } else if (result.actualResult is VoiceCommandResult.Error) {
                    println("   Error: ${result.actualResult.message}")
                }
                println()
            }
        } else {
            println("\n🎉 ALL TESTS PASSED! 🎉")
        }

        // Assertion: We want at least 90% success rate
        assertThat(successCount.toFloat() / testCases.size).isAtLeast(0.90f)
    }

    data class TestCase(
        val command: String,
        val expectedType: String, // "Alarm" or "Timer"
        val expectedTime: LocalTime? = null,
        val expectedDuration: Long? = null
    )

    data class TestResult(
        val index: Int,
        val command: String,
        val expectedType: String,
        val actualResult: VoiceCommandResult,
        val success: Boolean
    )
}
