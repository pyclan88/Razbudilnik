package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallengeProgress
import javax.inject.Inject
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

class CreateInitialReaderChallengeProgressUseCase @Inject constructor() {

    operator fun invoke(): ReaderChallengeProgress {
        return ReaderChallengeProgress(
            activeReadingTime = ZERO,
            requiredReadingTime = REQUIRED_READING_TIME,
            isFingerDown = false,
            isFingerMoving = false,
        )
    }

    private companion object {
        val REQUIRED_READING_TIME = 10.seconds
    }
}
