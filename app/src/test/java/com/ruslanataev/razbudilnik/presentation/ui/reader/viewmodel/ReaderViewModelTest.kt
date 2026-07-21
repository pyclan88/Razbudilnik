package com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel

import com.ruslanataev.razbudilnik.domain.reader.usecases.CreateInitialReaderChallengeProgressUseCase
import com.ruslanataev.razbudilnik.domain.reader.usecases.GetReaderChallengeUseCase
import com.ruslanataev.razbudilnik.domain.reader.usecases.UpdateReaderChallengeProgressUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class ReaderViewModelTest {

    private fun createViewModel(): ReaderViewModel {
        return ReaderViewModel(
            getReaderChallengeUseCase = GetReaderChallengeUseCase(),
            createInitialReaderChallengeProgressUseCase = CreateInitialReaderChallengeProgressUseCase(),
            updateReaderChallengeProgressUseCase = UpdateReaderChallengeProgressUseCase(),
        )
    }

    @Test
    fun `starts on first page`() {
        val viewModel = createViewModel()

        val state = viewModel.state.value

        assertEquals(1, state.pageNumber)
        assertEquals(3, state.pageCount)
        assertFalse(state.canGoToPreviousPage)
        assertFalse(state.canGoToNextPage)
        assertFalse(state.canFinishChallenge)
    }

    @Test
    fun `does not move to next page before current page is completed`() {
        val viewModel = createViewModel()

        viewModel.onNextPageClick()

        val state = viewModel.state.value

        assertEquals(1, state.pageNumber)
    }

    @Test
    fun `moves to next page after current page is completed`() {
        val viewModel = createViewModel()

        viewModel.onReadingInteractionTick(
            elapsedTime = 10.seconds,
            isFingerDown = true,
            isFingerMoving = true,
        )
        viewModel.onNextPageClick()

        val state = viewModel.state.value

        assertEquals(2, state.pageNumber)
        assertTrue(state.canGoToPreviousPage)
        assertEquals(0.seconds, state.activeReadingTime)
    }

    @Test
    fun `keeps completed progress when returning to previous page`() {
        val viewModel = createViewModel()

        viewModel.onReadingInteractionTick(
            elapsedTime = 10.seconds,
            isFingerDown = true,
            isFingerMoving = true,
        )
        viewModel.onNextPageClick()
        viewModel.onPreviousPageClick()

        val state = viewModel.state.value

        assertEquals(1, state.pageNumber)
        assertEquals(10.seconds, state.activeReadingTime)
        assertTrue(state.canGoToNextPage)
    }

    @Test
    fun `shows finish action after final page is completed`() {
        val viewModel = createViewModel()

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

        val state = viewModel.state.value

        assertEquals(3, state.pageNumber)
        assertFalse(state.canGoToNextPage)
        assertTrue(state.canFinishChallenge)
    }
}