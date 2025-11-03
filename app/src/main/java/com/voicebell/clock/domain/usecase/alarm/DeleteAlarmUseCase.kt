package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for deleting an alarm.
 */
class DeleteAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: Long): Result<Unit> {
        return try {
            alarmRepository.deleteAlarm(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
