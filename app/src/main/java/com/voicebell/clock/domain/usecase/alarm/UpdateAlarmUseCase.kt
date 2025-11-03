package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for updating an existing alarm.
 */
class UpdateAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm): Result<Unit> {
        return try {
            // Recalculate next trigger time
            val nextTrigger = if (alarm.isEnabled) {
                alarm.getNextTriggerTime()
            } else {
                0
            }

            // Update alarm
            alarmRepository.updateAlarm(alarm)

            // Update next trigger time
            alarmRepository.updateNextTriggerTime(alarm.id, nextTrigger)

            // Reset snooze count when alarm is updated
            alarmRepository.resetSnoozeCount(alarm.id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
