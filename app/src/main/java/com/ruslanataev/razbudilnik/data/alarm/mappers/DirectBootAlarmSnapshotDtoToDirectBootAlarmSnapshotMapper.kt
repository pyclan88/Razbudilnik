package com.ruslanataev.razbudilnik.data.alarm.mappers

import com.ruslanataev.razbudilnik.data.alarm.models.DirectBootAlarmSnapshotDto
import com.ruslanataev.razbudilnik.domain.alarm.models.DirectBootAlarmSnapshot

object DirectBootAlarmSnapshotDtoToDirectBootAlarmSnapshotMapper {

    fun map(snapshot: DirectBootAlarmSnapshotDto): DirectBootAlarmSnapshot {
        return DirectBootAlarmSnapshot(
            hour = snapshot.hour,
            minute = snapshot.minute,
            enabled = snapshot.enabled,
        )
    }

}
