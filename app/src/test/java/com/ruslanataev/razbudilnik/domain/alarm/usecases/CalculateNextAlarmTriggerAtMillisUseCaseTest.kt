package com.ruslanataev.razbudilnik.domain.alarm.usecases

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class CalculateNextAlarmTriggerAtMillisUseCaseTest {

    private val zoneId = ZoneId.of("Europe/Moscow")

    @Test
    fun `returns today when alarm time is still ahead`() {
        val useCase = createUseCase(now = "2026-07-18T07:00:00Z")

        val result = useCase(hour = 11, minute = 30)

        val expected = millisFor(year = 2026, month = 7, day = 18, hour = 11, minute = 30)
        assertEquals(expected, result)
    }

    @Test
    fun `returns tomorrow when alarm time passed today`() {
        val useCase = createUseCase(now = "2026-07-18T09:00:00Z")

        val result = useCase(hour = 7, minute = 30)

        val expected = millisFor(year = 2026, month = 7, day = 19, hour = 7, minute = 30)
        assertEquals(expected, result)
    }

    @Test
    fun `returns tomorrow when alarm equals current time`() {
        val useCase = createUseCase(now = "2026-07-18T04:30:00Z")

        val result = useCase(hour = 7, minute = 30)

        val expected = millisFor(year = 2026, month = 7, day = 19, hour = 7, minute = 30)
        assertEquals(expected, result)
    }

    private fun createUseCase(now: String): CalculateNextAlarmTriggerAtMillisUseCase {
        val clock = Clock.fixed(
            Instant.parse(now),
            zoneId,
        )

        return CalculateNextAlarmTriggerAtMillisUseCase(clock = clock)
    }

    private fun millisFor(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long {
        return LocalDateTime.of(year, month, day, hour, minute)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }
}
