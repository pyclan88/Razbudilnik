package com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel

import com.ruslanataev.razbudilnik.MainDispatcherRule
import com.ruslanataev.razbudilnik.domain.reader.api.ReaderBookRepository
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBookIds
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderPage
import com.ruslanataev.razbudilnik.domain.reader.usecases.CreateInitialReaderChallengeProgressUseCase
import com.ruslanataev.razbudilnik.domain.reader.usecases.GetReaderChallengeUseCase
import com.ruslanataev.razbudilnik.domain.reader.usecases.UpdateReaderChallengeProgressUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val readerBookRepository: ReaderBookRepository = mockk()

    private val testBook = ReaderBook(
        id = ReaderBookIds.CAUCASIAN_PRISONER,
        title = "Test book",
        author = "Test author",
        pages = List(3) { index ->
            ReaderPage(
                id = "page-$index",
                text = "Page ${index + 1}",
            )
        },
    )

    private fun createViewModel(): ReaderViewModel {
        coEvery {
            readerBookRepository.getBook(ReaderBookIds.CAUCASIAN_PRISONER)
        } returns testBook

        return ReaderViewModel(
            getReaderChallengeUseCase = GetReaderChallengeUseCase(
                readerBookRepository = readerBookRepository
            ),
            createInitialReaderChallengeProgressUseCase = CreateInitialReaderChallengeProgressUseCase(),
            updateReaderChallengeProgressUseCase = UpdateReaderChallengeProgressUseCase(),
        )
    }

    @Test
    fun `starts on first page`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = requireNotNull(viewModel.state.value)

        assertEquals(1, state.pageNumber)
        assertEquals(3, state.pageCount)
        assertFalse(state.canGoToPreviousPage)
        assertFalse(state.canGoToNextPage)
        assertFalse(state.canFinishChallenge)
    }

    @Test
    fun `does not move to next page before current page is completed`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onNextPageClick()

        val state = requireNotNull(viewModel.state.value)

        assertEquals(1, state.pageNumber)
    }

    @Test
    fun `moves to next page after current page is completed`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onReadingInteractionTick(
            elapsedTime = 10.seconds,
            isFingerDown = true,
            isFingerMoving = true,
        )
        viewModel.onNextPageClick()

        val state = requireNotNull(viewModel.state.value)

        assertEquals(2, state.pageNumber)
        assertTrue(state.canGoToPreviousPage)
        assertEquals(0.seconds, state.activeReadingTime)
    }

    @Test
    fun `keeps completed progress when returning to previous page`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onReadingInteractionTick(
            elapsedTime = 10.seconds,
            isFingerDown = true,
            isFingerMoving = true,
        )
        viewModel.onNextPageClick()
        viewModel.onPreviousPageClick()

        val state = requireNotNull(viewModel.state.value)

        assertEquals(1, state.pageNumber)
        assertEquals(10.seconds, state.activeReadingTime)
        assertTrue(state.canGoToNextPage)
    }

    @Test
    fun `shows finish action after final page is completed`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        repeat(2) {
            viewModel.onReadingInteractionTick(
                elapsedTime = 10.seconds,
                isFingerDown = true,
                isFingerMoving = true,
            )
            viewModel.onNextPageClick()
        }

        viewModel.onReadingInteractionTick(
            elapsedTime = 10.seconds,
            isFingerDown = true,
            isFingerMoving = true,
        )

        val state = requireNotNull(viewModel.state.value)

        assertEquals(3, state.pageNumber)
        assertFalse(state.canGoToNextPage)
        assertTrue(state.canFinishChallenge)
    }
}