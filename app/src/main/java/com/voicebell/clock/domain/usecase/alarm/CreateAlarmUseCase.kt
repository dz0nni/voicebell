package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for creating a new alarm.
 *
 * @return The ID of the created alarm.
 */
class CreateAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm): Result<Long> {
        return try {
            // Calculate next trigger time
            val nextTrigger = alarm.getNextTriggerTime()

            // Validate alarm
            if (nextTrigger <= 0) {
                return Result.failure(IllegalArgumentException("Invalid alarm time"))
            }

            // Create alarm
            val alarmId = alarmRepository.createAlarm(alarm)

            // Update next trigger time
            alarmRepository.updateNextTriggerTime(alarmId, nextTrigger)

            Result.success(alarmId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
