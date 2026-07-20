package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallengeProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class UpdateReaderChallengeProgressUseCaseTest {

    private val useCase = UpdateReaderChallengeProgressUseCase()

    @Test
    fun `adds elapsed time when finger is down and moving`() {
        val progress = createProgress(activeReadingSeconds = 10)

        val result = useCase(
            progress = progress,
            elapsedTime = 5.seconds,
            isFingerDown = true,
            isFingerMoving = true,
        )

        assertEquals(15.seconds, result.activeReadingTime)
        assertTrue(result.isFingerDown)
        assertTrue(result.isFingerMoving)
        assertTrue(result.shouldMuteAlarm)
    }

    @Test
    fun `does not add elapsed time when finger is lifted`() {
        val progress = createProgress(activeReadingSeconds = 10)

        val result = useCase(
            progress = progress,
            elapsedTime = 5.seconds,
            isFingerDown = false,
            isFingerMoving = true,
        )

        assertEquals(10.seconds, result.activeReadingTime)
        assertFalse(result.isFingerDown)
        assertTrue(result.isFingerMoving)
        assertFalse(result.shouldMuteAlarm)
    }

    @Test
    fun `does not add elapsed time when finger is down but not moving`() {
        val progress = createProgress(activeReadingSeconds = 10)

        val result = useCase(
            progress = progress,
            elapsedTime = 5.seconds,
            isFingerDown = true,
            isFingerMoving = false,
        )

        assertEquals(10.seconds, result.activeReadingTime)
        assertTrue(result.isFingerDown)
        assertFalse(result.isFingerMoving)
        assertFalse(result.shouldMuteAlarm)
    }

    @Test
    fun `allows next page when active reading time reaches required reading time`() {
        val progress = createProgress(activeReadingSeconds = 175)

        val result = useCase(
            progress = progress,
            elapsedTime = 5.seconds,
            isFingerDown = true,
            isFingerMoving = true,
        )

        assertEquals(180.seconds, result.activeReadingTime)
        assertTrue(result.canGoToNextPage)
    }

    private fun createProgress(activeReadingSeconds: Int): ReaderChallengeProgress {
        return ReaderChallengeProgress(
            activeReadingTime = activeReadingSeconds.seconds,
            requiredReadingTime = 3.minutes,
            isFingerDown = false,
            isFingerMoving = false,
        )
    }
}
