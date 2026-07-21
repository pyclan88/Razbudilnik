package com.ruslanataev.razbudilnik.presentation.ui.reader.states

import kotlin.time.Duration

data class ReaderUiState(
    val pageText: String,
    val pageNumber: Int,
    val pageCount: Int,
    val activeReadingTime: Duration,
    val requiredReadingTime: Duration,
    val canGoToPreviousPage: Boolean,
    val canGoToNextPage: Boolean,
    val shouldMuteAlarm: Boolean,
) {

    val progressText: String
        get() = "${activeReadingTime.inWholeSeconds}/${requiredReadingTime.inWholeSeconds} sec"

    val pageTextLabel: String
        get() = "$pageNumber / $pageCount"
}
