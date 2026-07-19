package com.ruslanataev.razbudilnik.domain.alarm.usecases

import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import com.ruslanataev.razbudilnik.domain.alarm.api.DirectBootAlarmSnapshotRepository
import javax.inject.Inject

class RescheduleDirectBootAlarmUseCase @Inject constructor(
    private val directBootAlarmSnapshotRepository: DirectBootAlarmSnapshotRepository,
    private val alarmScheduler: AlarmScheduler,
) {

    suspend operator fun invoke(): Boolean {
        val snapshot = directBootAlarmSnapshotRepository.get() ?: return false

        if (!snapshot.enabled) {
            return false
        }

        return alarmScheduler.schedule(
            hour = snapshot.hour,
            minute = snapshot.minute,
        )
    }
}
