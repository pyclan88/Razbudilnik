package com.ruslanataev.razbudilnik.data.alarm.mappers

import com.ruslanataev.razbudilnik.data.alarm.models.DirectBootAlarmSnapshotDto
import com.ruslanataev.razbudilnik.domain.alarm.models.DirectBootAlarmSnapshot

object DirectBootAlarmSnapshotToDirectBootAlarmSnapshotDtoMapper {

    fun map(snapshot: DirectBootAlarmSnapshot): DirectBootAlarmSnapshotDto {
        return DirectBootAlarmSnapshotDto(
            hour = snapshot.hour,
            minute = snapshot.minute,
            enabled = snapshot.enabled,
        )
    }
}
