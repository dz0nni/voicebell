package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import com.voicebell.clock.util.AlarmScheduler
import javax.inject.Inject

/**
 * Use case for scheduling an alarm with AlarmManager.
 *
 * This use case:
 * 1. Updates the alarm in database
 * 2. Calculates next trigger time
 * 3. Schedules with AlarmManager
 */
class ScheduleAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm): Result<Unit> {
        return try {
            // Check permission first
            if (!alarmScheduler.canScheduleExactAlarms()) {
                return Result.failure(SecurityException("Cannot schedule exact alarms"))
            }

            // Calculate next trigger time
            val nextTrigger = alarm.getNextTriggerTime()
            if (nextTrigger <= 0) {
                return Result.failure(IllegalArgumentException("Invalid alarm time"))
            }

            // Update alarm in database with next trigger time
            alarmRepository.updateAlarm(alarm)
            alarmRepository.updateNextTriggerTime(alarm.id, nextTrigger)

            // Schedule with AlarmManager
            alarmScheduler.scheduleAlarm(alarm)

            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancel alarm scheduling.
     */
    suspend fun cancel(alarmId: Long): Result<Unit> {
        return try {
            alarmScheduler.cancelAlarm(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
