package com.voicebell.clock.data.mapper

import com.voicebell.clock.data.local.database.entities.AlarmEntity
import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.model.AlarmTone
import com.voicebell.clock.domain.model.DayOfWeek
import java.time.LocalTime

/**
 * Mapper functions to convert between AlarmEntity (data layer) and Alarm (domain layer).
 */

/**
 * Convert AlarmEntity to Alarm (domain model).
 */
fun AlarmEntity.toDomain(): Alarm {
    return Alarm(
        id = id,
        time = LocalTime.of(hour, minute),
        isEnabled = isEnabled,
        label = label,
        alarmTone = AlarmTone.fromId(alarmTone),
        repeatDays = DayOfWeek.fromString(repeatDays),
        vibrate = vibrate,
        flash = flash,
        gradualVolumeIncrease = gradualVolumeIncrease,
        volumeLevel = volumeLevel,
        snoozeEnabled = snoozeEnabled,
        snoozeDuration = snoozeDuration,
        snoozeCount = snoozeCount,
        maxSnoozeCount = maxSnoozeCount,
        preAlarmCount = preAlarmCount,
        preAlarmInterval = preAlarmInterval,
        createdAt = createdAt
    )
}

/**
 * Convert Alarm (domain model) to AlarmEntity.
 */
fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        hour = time.hour,
        minute = time.minute,
        isEnabled = isEnabled,
        label = label,
        alarmTone = alarmTone.id,
        repeatDays = DayOfWeek.toString(repeatDays),
        vibrate = vibrate,
        flash = flash,
        gradualVolumeIncrease = gradualVolumeIncrease,
        volumeLevel = volumeLevel,
        snoozeEnabled = snoozeEnabled,
        snoozeDuration = snoozeDuration,
        snoozeCount = snoozeCount,
        maxSnoozeCount = maxSnoozeCount,
        preAlarmCount = preAlarmCount,
        preAlarmInterval = preAlarmInterval,
        createdAt = createdAt,
        nextTriggerTime = getNextTriggerTime()
    )
}

/**
 * Convert list of AlarmEntity to list of Alarm.
 */
fun List<AlarmEntity>.toDomain(): List<Alarm> {
    return map { it.toDomain() }
}
