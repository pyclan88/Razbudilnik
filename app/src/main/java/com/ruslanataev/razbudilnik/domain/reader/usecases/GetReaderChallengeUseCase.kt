package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallenge
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderPage
import javax.inject.Inject

class GetReaderChallengeUseCase @Inject constructor() {

    operator fun invoke(): ReaderChallenge {
        return ReaderChallenge(
            pages = listOf(
                ReaderPage(
                    text = PAGE_ONE_TEXT,
                ),
                ReaderPage(
                    text = PAGE_TWO_TEXT,
                ),
                ReaderPage(
                    text = PAGE_THREE_TEXT,
                ),
            ),
            currentPageIndex = 0,
        )
    }

    private companion object {
        const val PAGE_ONE_TEXT = "The morning was still dark when the first bell rang. " +
                "He opened his eyes and tried to remember why he had promised himself that today " +
                "would be different."

        const val PAGE_TWO_TEXT = "The room was quiet except for the sound waiting beside him. " +
                "It did not argue, explain, or forgive. It only waited for him to begin."

        const val PAGE_THREE_TEXT = "By the time he reached the final lines, sleep had lost its " +
                "grip. Not dramatically. Not heroically. Just enough for the day to start."
    }
}
