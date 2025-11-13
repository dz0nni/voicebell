package com.voicebell.clock.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Domain model representing an alarm.
 *
 * This is the business logic model, independent of database or UI.
 */
data class Alarm(
    val id: Long = 0,
    val time: LocalTime,
    val isEnabled: Boolean = true,
    val label: String = "",
    val alarmTone: AlarmTone = AlarmTone.DEFAULT,
    val repeatDays: Set<com.voicebell.clock.domain.model.DayOfWeek> = emptySet(),
    val vibrate: Boolean = true,
    val flash: Boolean = false,
    val gradualVolumeIncrease: Boolean = true,
    val volumeLevel: Int = 80,
    val snoozeEnabled: Boolean = true,
    val snoozeDuration: Int = 10,
    val snoozeCount: Int = 0,
    val maxSnoozeCount: Int = 3,
    val preAlarmCount: Int = 0,
    val preAlarmInterval: Int = 7,
    val createdAt: Long = System.currentTimeMillis()
) {

    /**
     * Get next trigger time in milliseconds.
     */
    fun getNextTriggerTime(from: LocalDateTime = LocalDateTime.now()): Long {
        val zoneId = ZoneId.systemDefault()
        val currentDateTime = from
        val currentTime = currentDateTime.toLocalTime()
        val alarmTime = time

        // If no repeat days, alarm is one-time
        if (repeatDays.isEmpty()) {
            val alarmDateTime = if (alarmTime.isAfter(currentTime)) {
                // Today
                LocalDateTime.of(currentDateTime.toLocalDate(), alarmTime)
            } else {
                // Tomorrow
                LocalDateTime.of(currentDateTime.toLocalDate().plusDays(1), alarmTime)
            }
            return ZonedDateTime.of(alarmDateTime, zoneId).toInstant().toEpochMilli()
        }

        // With repeat days, find next occurrence
        val currentDayOfWeek = currentDateTime.dayOfWeek
        val sortedDays = repeatDays.sortedBy { it.value }

        // Check if alarm should trigger today
        val todayDomainDay = javaDayOfWeekToDomain(currentDayOfWeek)
        if (todayDomainDay in repeatDays && alarmTime.isAfter(currentTime)) {
            val alarmDateTime = LocalDateTime.of(currentDateTime.toLocalDate(), alarmTime)
            return ZonedDateTime.of(alarmDateTime, zoneId).toInstant().toEpochMilli()
        }

        // Find next day in repeat days
        for (i in 1..7) {
            val nextDate = currentDateTime.toLocalDate().plusDays(i.toLong())
            val nextDayOfWeek = javaDayOfWeekToDomain(nextDate.dayOfWeek)
            if (nextDayOfWeek in repeatDays) {
                val alarmDateTime = LocalDateTime.of(nextDate, alarmTime)
                return ZonedDateTime.of(alarmDateTime, zoneId).toInstant().toEpochMilli()
            }
        }

        // Should never reach here if repeatDays is not empty
        return 0
    }

    /**
     * Get human-readable time until alarm.
     */
    fun getTimeUntilAlarm(from: LocalDateTime = LocalDateTime.now()): String {
        val nextTrigger = getNextTriggerTime(from)
        val now = ZonedDateTime.now().toInstant().toEpochMilli()
        val diff = nextTrigger - now

        val hours = ChronoUnit.HOURS.between(
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(now), ZoneId.systemDefault()),
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(nextTrigger), ZoneId.systemDefault())
        )
        val minutes = (diff / 60000) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    /**
     * Get formatted time string (e.g., "07:30 AM" or "19:30" in 24h format).
     */
    fun getFormattedTime(use24Hour: Boolean = false): String {
        return if (use24Hour) {
            String.format("%02d:%02d", time.hour, time.minute)
        } else {
            val hour12 = if (time.hour == 0) 12 else if (time.hour > 12) time.hour - 12 else time.hour
            val amPm = if (time.hour < 12) "AM" else "PM"
            String.format("%02d:%02d %s", hour12, time.minute, amPm)
        }
    }

    /**
     * Get repeat days display string.
     */
    fun getRepeatDaysString(): String {
        return when {
            repeatDays.isEmpty() -> "Once"
            repeatDays == com.voicebell.clock.domain.model.DayOfWeek.ALL_DAYS -> "Every day"
            repeatDays == com.voicebell.clock.domain.model.DayOfWeek.WEEKDAYS -> "Weekdays"
            repeatDays == com.voicebell.clock.domain.model.DayOfWeek.WEEKENDS -> "Weekends"
            else -> repeatDays.sortedBy { it.value }
                .joinToString(", ") { it.shortName }
        }
    }

    /**
     * Check if alarm can be snoozed.
     */
    fun canSnooze(): Boolean {
        return snoozeEnabled && snoozeCount < maxSnoozeCount
    }

    private fun javaDayOfWeekToDomain(javaDayOfWeek: DayOfWeek): com.voicebell.clock.domain.model.DayOfWeek {
        return when (javaDayOfWeek) {
            DayOfWeek.MONDAY -> com.voicebell.clock.domain.model.DayOfWeek.MONDAY
            DayOfWeek.TUESDAY -> com.voicebell.clock.domain.model.DayOfWeek.TUESDAY
            DayOfWeek.WEDNESDAY -> com.voicebell.clock.domain.model.DayOfWeek.WEDNESDAY
            DayOfWeek.THURSDAY -> com.voicebell.clock.domain.model.DayOfWeek.THURSDAY
            DayOfWeek.FRIDAY -> com.voicebell.clock.domain.model.DayOfWeek.FRIDAY
            DayOfWeek.SATURDAY -> com.voicebell.clock.domain.model.DayOfWeek.SATURDAY
            DayOfWeek.SUNDAY -> com.voicebell.clock.domain.model.DayOfWeek.SUNDAY
        }
    }
}
