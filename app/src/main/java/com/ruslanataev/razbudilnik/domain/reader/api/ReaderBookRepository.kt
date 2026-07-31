package com.ruslanataev.razbudilnik.domain.reader.api

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook

interface ReaderBookRepository {

    suspend fun getBook(bookId: String): ReaderBook
}
