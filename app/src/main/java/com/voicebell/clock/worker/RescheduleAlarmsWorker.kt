package com.voicebell.clock.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voicebell.clock.domain.repository.AlarmRepository
import com.voicebell.clock.util.AlarmScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker for rescheduling all alarms after device boot.
 */
@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "RescheduleAlarmsWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Rescheduling all alarms after boot")

            // Get all enabled alarms
            val alarms = alarmRepository.getEnabledAlarms().first()

            Log.d(TAG, "Found ${alarms.size} enabled alarms to reschedule")

            // Reschedule each alarm
            alarms.forEach { alarm ->
                try {
                    alarmScheduler.scheduleAlarm(alarm)
                    Log.d(TAG, "Rescheduled alarm: ${alarm.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alarm ${alarm.id}", e)
                }
            }

            Log.d(TAG, "Alarm rescheduling completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling alarms", e)
            Result.failure()
        }
    }
}
