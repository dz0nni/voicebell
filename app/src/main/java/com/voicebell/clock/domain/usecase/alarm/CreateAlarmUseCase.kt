package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import com.voicebell.clock.util.AlarmScheduler
import javax.inject.Inject

/**
 * Use case for creating a new alarm.
 *
 * @return The ID of the created alarm.
 */
class CreateAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
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

            // Schedule alarm with AlarmManager
            val createdAlarm = alarm.copy(id = alarmId)
            alarmScheduler.scheduleAlarm(createdAlarm)

            Result.success(alarmId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
