package com.voicebell.clock.domain.model

/**
 * Days of the week for alarm repeat functionality.
 */
enum class DayOfWeek(val value: Int, val displayName: String, val shortName: String) {
    MONDAY(1, "Monday", "Mon"),
    TUESDAY(2, "Tuesday", "Tue"),
    WEDNESDAY(3, "Wednesday", "Wed"),
    THURSDAY(4, "Thursday", "Thu"),
    FRIDAY(5, "Friday", "Fri"),
    SATURDAY(6, "Saturday", "Sat"),
    SUNDAY(7, "Sunday", "Sun");

    companion object {
        fun fromValue(value: Int): DayOfWeek? {
            return values().firstOrNull { it.value == value }
        }

        fun fromString(str: String): Set<DayOfWeek> {
            if (str.isEmpty()) return emptySet()
            return str.split(",")
                .mapNotNull { it.toIntOrNull()?.let { value -> fromValue(value) } }
                .toSet()
        }

        fun toString(days: Set<DayOfWeek>): String {
            return days.sortedBy { it.value }
                .joinToString(",") { it.value.toString() }
        }

        val WEEKDAYS = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        val WEEKENDS = setOf(SATURDAY, SUNDAY)
        val ALL_DAYS = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    }
}
