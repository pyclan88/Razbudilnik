package com.ruslanataev.razbudilnik.domain.reader.usecases

import com.ruslanataev.razbudilnik.domain.reader.api.ReaderBookRepository
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBookIds
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderPage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

class GetReaderBookUseCaseTest {

    private val readerBookRepository: ReaderBookRepository = mockk()
    private val useCase = GetReaderBookUseCase(readerBookRepository)

    private val testBook = ReaderBook(
        id = ReaderBookIds.CAUCASIAN_PRISONER,
        title = "Test book",
        author = "Test author",
        text = "Test book text",
        pages = List(3) { index ->
            ReaderPage(
                id = "page-$index",
                text = "Page ${index + 1}",
            )
        },
    )

    @Test
    fun `returns requested book from repository`() = runTest {
        coEvery {
            readerBookRepository.getBook(ReaderBookIds.CAUCASIAN_PRISONER)
        } returns testBook

        val actualBook = useCase.invoke(ReaderBookIds.CAUCASIAN_PRISONER)

        assertSame(testBook, actualBook)

        coVerify(exactly = 1) {
            readerBookRepository.getBook(ReaderBookIds.CAUCASIAN_PRISONER)
        }
    }
}
