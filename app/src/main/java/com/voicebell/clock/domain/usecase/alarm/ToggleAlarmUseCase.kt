package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for toggling alarm enabled state.
 */
class ToggleAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: Long, isEnabled: Boolean): Result<Unit> {
        return try {
            // Toggle enabled state
            alarmRepository.toggleAlarmEnabled(alarmId, isEnabled)

            // Get updated alarm to calculate next trigger
            val alarm = alarmRepository.getAlarmById(alarmId)
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Update next trigger time
            val nextTrigger = if (isEnabled) {
                alarm.getNextTriggerTime()
            } else {
                0
            }
            alarmRepository.updateNextTriggerTime(alarmId, nextTrigger)

            // Reset snooze count when toggling
            alarmRepository.resetSnoozeCount(alarmId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
