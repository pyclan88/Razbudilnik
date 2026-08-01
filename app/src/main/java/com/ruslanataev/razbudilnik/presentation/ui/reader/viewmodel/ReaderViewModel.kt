package com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getReaderChallengeUseCase: GetReaderChallengeUseCase,
    private val createInitialReaderChallengeProgressUseCase: CreateInitialReaderChallengeProgressUseCase,
    private val updateReaderChallengeProgressUseCase: UpdateReaderChallengeProgressUseCase,
) : ViewModel() {

    private var challenge: ReaderChallenge? = null
    private var progressByPageIndex: List<ReaderChallengeProgress> = emptyList()

    private val _state: MutableStateFlow<ReaderUiState?> = MutableStateFlow(null)
    val state: StateFlow<ReaderUiState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadChallenge()
        }
    }

    fun onReadingInteractionTick(
        elapsedTime: Duration,
        isFingerDown: Boolean,
        isFingerMoving: Boolean,
    ) {
        val currentChallenge = challenge ?: return
        val currentPageIndex = currentChallenge.currentPageIndex
        val currentProgress = progressByPageIndex[currentPageIndex]

        val updatedProgress = updateReaderChallengeProgressUseCase(
            progress = currentProgress,
            elapsedTime = elapsedTime,
            isFingerDown = isFingerDown,
            isFingerMoving = isFingerMoving,
        )

        progressByPageIndex = progressByPageIndex.mapIndexed { index, progress ->
            if (index == currentPageIndex) {
                updatedProgress
            } else {
                progress
            }
        }

        updateState()
    }

    fun onNextPageClick() {
        val currentChallenge = challenge ?: return
        val currentProgress = progressByPageIndex[currentChallenge.currentPageIndex]

        if (!currentProgress.canGoToNextPage) {
            return
        }

        val nextPageIndex = currentChallenge.currentPageIndex + 1

        if (nextPageIndex >= currentChallenge.pages.size) {
            return
        }

        challenge = currentChallenge.copy(currentPageIndex = nextPageIndex)
        updateState()
    }

    fun onPreviousPageClick() {
        val currentChallenge = challenge ?: return
        val previousPageIndex = currentChallenge.currentPageIndex - 1

        if (previousPageIndex < 0) {
            return
        }

        challenge = currentChallenge.copy(currentPageIndex = previousPageIndex)
        updateState()
    }

    private suspend fun loadChallenge() {
        val loadedChallenge = getReaderChallengeUseCase()

        challenge = loadedChallenge
        progressByPageIndex = loadedChallenge.pages.map {
            createInitialReaderChallengeProgressUseCase()
        }

        updateState()
    }

    private fun updateState() {
        val currentChallenge = challenge ?: return

        _state.value = ReaderChallengeToReaderUiStateMapper.map(
            challenge = currentChallenge,
            progress = progressByPageIndex[currentChallenge.currentPageIndex],
        )
    }
}
