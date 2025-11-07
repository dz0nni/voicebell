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
}
