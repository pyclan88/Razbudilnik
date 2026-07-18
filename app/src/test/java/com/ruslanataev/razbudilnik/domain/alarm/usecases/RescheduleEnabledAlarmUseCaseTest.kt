package com.ruslanataev.razbudilnik.domain.alarm.usecases

import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import com.ruslanataev.razbudilnik.domain.setup.models.AlarmSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescheduleEnabledAlarmUseCaseTest {

    private val alarmSettingsRepository = mockk<AlarmSettingsRepository>()
    private val alarmScheduler = mockk<AlarmScheduler>()

    private val useCase = RescheduleEnabledAlarmUseCase(
        alarmSettingsRepository = alarmSettingsRepository,
        alarmScheduler = alarmScheduler,
    )

    @Test
    fun `schedules saved time when alarm is enabled`() = runTest {
        every { alarmSettingsRepository.settings } returns flowOf(
            AlarmSettings(hour = 7, minute = 30, enabled = true),
        )
        coEvery { alarmScheduler.schedule(hour = 7, minute = 30) } returns true

        val result = useCase()

        assertTrue(result)
        coVerify(exactly = 1) {
            alarmScheduler.schedule(hour = 7, minute = 30)
        }
    }

    @Test
    fun `does not schedule when alarm is disabled`() = runTest {
        every { alarmSettingsRepository.settings } returns flowOf(
            AlarmSettings(hour = 7, minute = 30, enabled = false),
        )

        val result = useCase()

        assertFalse(result)
        coVerify(exactly = 0) {
            alarmScheduler.schedule(any(), any())
        }
    }

    @Test
    fun `returns false when scheduler cannot schedule enabled alarm`() = runTest {
        every { alarmSettingsRepository.settings } returns flowOf(
            AlarmSettings(hour = 7, minute = 30, enabled = true),
        )
        coEvery { alarmScheduler.schedule(hour = 7, minute = 30) } returns false

        val result = useCase()

        assertFalse(result)
        coVerify(exactly = 1) {
            alarmScheduler.schedule(hour = 7, minute = 30)
        }
    }
}
