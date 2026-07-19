package com.ruslanataev.razbudilnik.domain.alarm.api

import com.ruslanataev.razbudilnik.domain.alarm.models.DirectBootAlarmSnapshot

interface DirectBootAlarmSnapshotRepository {

    suspend fun save(snapshot: DirectBootAlarmSnapshot)

    suspend fun get(): DirectBootAlarmSnapshot?
}
