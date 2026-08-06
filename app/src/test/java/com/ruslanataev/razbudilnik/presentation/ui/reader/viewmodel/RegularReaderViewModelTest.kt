package com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel

import com.ruslanataev.razbudilnik.MainDispatcherRule
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBookIds
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderPage
import com.ruslanataev.razbudilnik.domain.reader.usecases.GetReaderBookUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegularReaderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase: GetReaderBookUseCase = mockk()

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
    fun `loads canonical book into initial state`() = runTest {
        coEvery {
            useCase.invoke(ReaderBookIds.CAUCASIAN_PRISONER)
        } returns testBook

        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = requireNotNull(viewModel.state.value)

        assertEquals(testBook.id, state.bookId)
        assertEquals(testBook.title, state.bookTitle)
        assertEquals(testBook.author, state.bookAuthor)
        assertEquals(testBook.text, state.bookText)
        assertEquals(0, state.currentViewOffset)
        assertEquals(0, state.readingProgressOffset)
    }

    private fun createViewModel(): RegularReaderViewModel {
        return RegularReaderViewModel(
            getReaderBookUseCase = useCase,
        )
    }
}
