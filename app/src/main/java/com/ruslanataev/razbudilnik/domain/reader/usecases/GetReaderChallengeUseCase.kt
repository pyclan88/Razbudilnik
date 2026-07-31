package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallenge
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderPage
import javax.inject.Inject

class GetReaderChallengeUseCase @Inject constructor() {

    operator fun invoke(): ReaderChallenge {
        return ReaderChallenge(
            book = ReaderBook(
                id = SAMPLE_BOOK_ID,
                title = SAMPLE_BOOK_TITLE,
                author = SAMPLE_BOOK_AUTHOR,
                pages = listOf(
                    ReaderPage(
                        id = "$SAMPLE_BOOK_ID-page-1",
                        text = PAGE_ONE_TEXT,
                    ),
                    ReaderPage(
                        id = "$SAMPLE_BOOK_ID-page-2",
                        text = PAGE_TWO_TEXT,
                    ),
                    ReaderPage(
                        id = "$SAMPLE_BOOK_ID-page-3",
                        text = PAGE_THREE_TEXT,
                    ),
                ),
            ),
            firstPageIndex = 0,
            requiredPageCount = DEFAULT_CHALLENGE_PAGE_COUNT,
            currentPageIndex = 0,
        )
    }

    private companion object {
        const val DEFAULT_CHALLENGE_PAGE_COUNT = 5
        const val SAMPLE_BOOK_ID = "reader-mvp-sample"
        const val SAMPLE_BOOK_TITLE = "Reader MVP Sample"
        const val SAMPLE_BOOK_AUTHOR = "Razbudilnik"

        const val PAGE_ONE_TEXT = "The morning was still dark when the first bell rang. " +
                "He opened his eyes and tried to remember why he had promised himself that today " +
                "would be different."

        const val PAGE_TWO_TEXT = "The room was quiet except for the sound waiting beside him. " +
                "It did not argue, explain, or forgive. It only waited for him to begin."

        const val PAGE_THREE_TEXT = "By the time he reached the final lines, sleep had lost its " +
                "grip. Not dramatically. Not heroically. Just enough for the day to start."
    }
}
