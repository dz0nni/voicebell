package com.voicebell.clock.domain.model

/**
 * Available alarm tones.
 */
enum class AlarmTone(val id: String, val displayName: String) {
    DEFAULT("default", "Default"),
    GENTLE("gentle", "Gentle Wake"),
    CLASSIC("classic", "Classic Bell"),
    NATURE("nature", "Nature Sounds"),
    DIGITAL("digital", "Digital Beep"),
    CHIMES("chimes", "Chimes");

    companion object {
        fun fromId(id: String): AlarmTone {
            return values().firstOrNull { it.id == id } ?: DEFAULT
        }
    }
}
