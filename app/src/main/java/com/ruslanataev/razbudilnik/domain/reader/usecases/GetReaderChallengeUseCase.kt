package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.api.ReaderBookRepository
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBookIds
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallenge
import javax.inject.Inject

class GetReaderChallengeUseCase @Inject constructor(
    private val readerBookRepository: ReaderBookRepository,
) {

    suspend operator fun invoke(): ReaderChallenge {
        val book = readerBookRepository.getBook(
            bookId = ReaderBookIds.CAUCASIAN_PRISONER,
        )

        return ReaderChallenge(
            book = book,
            firstPageIndex = 0,
            requiredPageCount = DEFAULT_CHALLENGE_PAGE_COUNT,
            currentPageIndex = 0,
        )
    }

    private companion object {
        const val DEFAULT_CHALLENGE_PAGE_COUNT = 5
    }
}
