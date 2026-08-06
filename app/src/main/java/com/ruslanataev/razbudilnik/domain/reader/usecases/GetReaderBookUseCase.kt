package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.api.ReaderBookRepository
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook
import javax.inject.Inject

class GetReaderBookUseCase @Inject constructor(
    private val readerBookRepository: ReaderBookRepository,
) {

    suspend operator fun invoke(bookId: String): ReaderBook {
        return readerBookRepository.getBook(bookId)
    }
}
