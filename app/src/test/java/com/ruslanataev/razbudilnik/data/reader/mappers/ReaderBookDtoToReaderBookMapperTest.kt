package com.ruslanataev.razbudilnik.data.reader.mappers

import com.ruslanataev.razbudilnik.data.reader.models.ReaderBookDto
import com.ruslanataev.razbudilnik.data.reader.models.ReaderPageDto
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderPage
import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderBookDtoToReaderBookMapperTest {

    @Test
    fun `maps reader book dto to reader book`() {
        val bookText = "Complete canonical book text"

        val readerBookDto = ReaderBookDto(
            id = "tolstoy-caucasian-prisoner",
            title = "Кавказский пленник",
            author = "Лев Толстой",
            text = bookText,
            pages = listOf(
                ReaderPageDto(
                    id = "page-001",
                    text = "First page",
                ),
                ReaderPageDto(
                    id = "page-002",
                    text = "Second page",
                ),
            ),
        )

        val expected = ReaderBook(
            id = "tolstoy-caucasian-prisoner",
            title = "Кавказский пленник",
            author = "Лев Толстой",
            text = bookText,
            pages = listOf(
                ReaderPage(
                    id = "page-001",
                    text = "First page",
                ),
                ReaderPage(
                    id = "page-002",
                    text = "Second page",
                ),
            ),
        )

        val actual = ReaderBookDtoToReaderBookMapper.map(readerBookDto)

        assertEquals(expected, actual)
    }
}
