package com.ruslanataev.razbudilnik.domain.alarm.api

interface AlarmScheduler {
    suspend fun schedule(hour: Int, minute: Int): Boolean

    suspend fun cancel()
}