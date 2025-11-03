package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all alarms.
 *
 * Returns a Flow that emits the list of alarms whenever it changes.
 */
class GetAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    operator fun invoke(): Flow<List<Alarm>> {
        return alarmRepository.getAllAlarms()
    }

    /**
     * Get only enabled alarms.
     */
    fun getEnabled(): Flow<List<Alarm>> {
        return alarmRepository.getEnabledAlarms()
    }
}
