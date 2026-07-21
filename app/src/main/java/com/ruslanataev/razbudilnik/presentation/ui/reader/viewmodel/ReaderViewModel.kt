package com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel

import androidx.lifecycle.ViewModel
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallenge
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallengeProgress
import com.ruslanataev.razbudilnik.domain.reader.usecases.CreateInitialReaderChallengeProgressUseCase
import com.ruslanataev.razbudilnik.domain.reader.usecases.GetReaderChallengeUseCase
import com.ruslanataev.razbudilnik.domain.reader.usecases.UpdateReaderChallengeProgressUseCase
import com.ruslanataev.razbudilnik.presentation.ui.reader.mappers.ReaderChallengeToReaderUiStateMapper
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.ReaderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class ReaderViewModel @Inject constructor(
    getReaderChallengeUseCase: GetReaderChallengeUseCase,
    private val createInitialReaderChallengeProgressUseCase: CreateInitialReaderChallengeProgressUseCase,
    private val updateReaderChallengeProgressUseCase: UpdateReaderChallengeProgressUseCase,
) : ViewModel() {

    private var challenge: ReaderChallenge = getReaderChallengeUseCase()
    private var progressByPageIndex: List<ReaderChallengeProgress> = challenge.pages.map {
        createInitialReaderChallengeProgressUseCase()
    }

    private val _state: MutableStateFlow<ReaderUiState> = MutableStateFlow(createUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    fun onReadingInteractionTick(
        elapsedTime: Duration,
        isFingerDown: Boolean,
        isFingerMoving: Boolean,
    ) {
        val currentPageIndex = challenge.currentPageIndex
        val currentProgress = progressByPageIndex[currentPageIndex]

        val updateProgress = updateReaderChallengeProgressUseCase(
            progress = currentProgress,
            elapsedTime = elapsedTime,
            isFingerDown = isFingerDown,
            isFingerMoving = isFingerMoving,
        )

        progressByPageIndex = progressByPageIndex.mapIndexed { index, progress ->
            if (index == currentPageIndex) {
                updateProgress
            } else {
                progress
            }
        }

        updateState()
    }

    fun onNextPageClick() {
        val currentProgress = progressByPageIndex[challenge.currentPageIndex]

        if (!currentProgress.canGoToNextPage) {
            return
        }

        val nextPageIndex = challenge.currentPageIndex + 1

        if (nextPageIndex >= challenge.pages.size) {
            return
        }

        challenge = challenge.copy(currentPageIndex = nextPageIndex)

        updateState()
    }

    fun onPreviousPageClick() {
        val previousPageIndex = challenge.currentPageIndex - 1

        if (previousPageIndex < 0) {
            return
        }

        challenge = challenge.copy(currentPageIndex = previousPageIndex)

        updateState()
    }

    private fun updateState() {
        _state.value = createUiState()
    }

    private fun createUiState(): ReaderUiState {
        return ReaderChallengeToReaderUiStateMapper.map(
            challenge = challenge,
            progress = progressByPageIndex[challenge.currentPageIndex],
        )
    }
}
