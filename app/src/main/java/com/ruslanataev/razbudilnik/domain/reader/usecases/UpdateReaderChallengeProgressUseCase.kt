package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallengeProgress
import javax.inject.Inject
import kotlin.time.Duration

class UpdateReaderChallengeProgressUseCase @Inject constructor() {

    operator fun invoke(
        progress: ReaderChallengeProgress,
        elapsedTime: Duration,
        isFingerDown: Boolean,
        isFingerMoving: Boolean,
    ): ReaderChallengeProgress {
        val shouldCountReadingTime = isFingerDown && isFingerMoving

        val activeReadingTime = if (shouldCountReadingTime) {
            progress.activeReadingTime + elapsedTime
        } else {
            progress.activeReadingTime
        }

        return progress.copy(
            activeReadingTime = activeReadingTime,
            isFingerDown = isFingerDown,
            isFingerMoving = isFingerMoving,
        )
    }
}
