package com.voicebell.clock.domain.usecase.alarm

import com.voicebell.clock.domain.model.Alarm
import com.voicebell.clock.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single alarm by ID.
 */
class GetAlarmByIdUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    /**
     * Get alarm by ID as Flow (reactive).
     */
    operator fun invoke(alarmId: Long): Flow<Alarm?> {
        return alarmRepository.getAlarmByIdFlow(alarmId)
    }

    /**
     * Get alarm by ID (single read).
     */
    suspend fun getOnce(alarmId: Long): Alarm? {
        return alarmRepository.getAlarmById(alarmId)
    }
}
