package com.ruslanataev.razbudilnik.presentation.ui.reader.states

import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

data class ReaderUiState(
    val pageText: String,
    val pageNumber: Int,
    val pageCount: Int,
    val activeReadingTime: Duration,
    val requiredReadingTime: Duration,
    val canGoToNextPage: Boolean,
    val shouldMuteAlarm: Boolean,
) {

    val progressText: String
        get() = "${activeReadingTime.inWholeSeconds}/${requiredReadingTime.inWholeSeconds} sec"

    val pageTextLabel: String
        get() = "$pageNumber / $pageCount"

    companion object {
        fun initial(): ReaderUiState {
            return ReaderUiState(
                pageText = "",
                pageNumber = 1,
                pageCount = 1,
                activeReadingTime = ZERO,
                requiredReadingTime = ZERO,
                canGoToNextPage = false,
                shouldMuteAlarm = false,
            )
        }
    }
}
