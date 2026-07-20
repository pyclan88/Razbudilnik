package com.ruslanataev.razbudilnik.domain.alarm.usecases

import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import com.ruslanataev.razbudilnik.domain.alarm.api.DirectBootAlarmSnapshotRepository
import com.ruslanataev.razbudilnik.domain.alarm.models.DirectBootAlarmSnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescheduleDirectBootAlarmUseCaseTest {

    private val snapshotRepository = mockk<DirectBootAlarmSnapshotRepository>()
    private val alarmScheduler = mockk<AlarmScheduler>()

    private val useCase = RescheduleDirectBootAlarmUseCase(
        directBootAlarmSnapshotRepository = snapshotRepository,
        alarmScheduler = alarmScheduler,
    )

    @Test
    fun `schedules alarm when snapshot is enabled`() = runTest {
        coEvery { snapshotRepository.get() } returns DirectBootAlarmSnapshot(
            hour = 7,
            minute = 30,
            enabled = true,
        )
        coEvery { alarmScheduler.schedule(hour = 7, minute = 30) } returns true

        val result = useCase()

        assertTrue(result)
        coVerify(exactly = 1) {
            alarmScheduler.schedule(hour = 7, minute = 30)
        }
    }

    @Test
    fun `does not schedule alarm when snapshot is disabled`() = runTest {
        coEvery { snapshotRepository.get() } returns DirectBootAlarmSnapshot(
            hour = 7,
            minute = 30,
            enabled = false,
        )

        val result = useCase()

        assertFalse(result)
        coVerify(exactly = 0) {
            alarmScheduler.schedule(any(), any())
        }
    }

    @Test
    fun `does not schedule alarm when snapshot is missing`() = runTest {
        coEvery { snapshotRepository.get() } returns null

        val result = useCase()

        assertFalse(result)
        coVerify(exactly = 0) {
            alarmScheduler.schedule(any(), any())
        }
    }
}
