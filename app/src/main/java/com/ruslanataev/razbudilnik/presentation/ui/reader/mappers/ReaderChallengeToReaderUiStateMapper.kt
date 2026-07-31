package com.ruslanataev.razbudilnik.presentation.ui.reader.mappers

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallenge
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallengeProgress
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.ReaderUiState

object ReaderChallengeToReaderUiStateMapper {

    fun map(
        challenge: ReaderChallenge,
        progress: ReaderChallengeProgress,
    ): ReaderUiState {
        val pages = challenge.pages
        val currentPage = pages[challenge.currentPageIndex]
        val isLastPage = challenge.currentPageIndex == pages.lastIndex

        return ReaderUiState(
            pageText = currentPage.text,
            pageNumber = challenge.currentPageIndex + 1,
            pageCount = pages.size,
            activeReadingTime = progress.activeReadingTime,
            requiredReadingTime = progress.requiredReadingTime,
            canGoToPreviousPage = challenge.currentPageIndex > 0,
            canGoToNextPage = progress.canGoToNextPage && !isLastPage,
            canFinishChallenge = progress.canGoToNextPage && isLastPage,
            shouldMuteAlarm = progress.shouldMuteAlarm,
        )
    }
}
