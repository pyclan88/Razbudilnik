package com.ruslanataev.razbudilnik.domain.reader.models

import kotlin.time.Duration

data class ReaderChallengeProgress(
    val activeReadingTime: Duration,
    val requiredReadingTime: Duration,
    val isFingerDown: Boolean,
    val isFingerMoving: Boolean,
) {

    val canGoToNextPage: Boolean
        get() = activeReadingTime >= requiredReadingTime

    val shouldMuteAlarm: Boolean
        get() = isFingerDown && isFingerMoving
}
