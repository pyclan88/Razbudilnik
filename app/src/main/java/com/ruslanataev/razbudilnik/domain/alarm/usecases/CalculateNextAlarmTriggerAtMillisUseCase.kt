package com.ruslanataev.razbudilnik.domain.alarm.usecases

import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class CalculateNextAlarmTriggerAtMillisUseCase @Inject constructor(
    private val clock: Clock,
) {

    operator fun invoke(hour: Int, minute: Int): Long {
        val zoneId = clock.zone
        val now = LocalDateTime.now(clock)

        var scheduledTime = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        if (!scheduledTime.isAfter(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        return scheduledTime
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }
}
