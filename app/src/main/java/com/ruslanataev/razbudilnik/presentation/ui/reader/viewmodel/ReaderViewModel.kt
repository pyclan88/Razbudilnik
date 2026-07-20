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
    private val getReaderChallengeUseCase: GetReaderChallengeUseCase,
    private val createInitialReaderChallengeProgressUseCase: CreateInitialReaderChallengeProgressUseCase,
    private val updateReaderChallengeProgressUseCase: UpdateReaderChallengeProgressUseCase,
) : ViewModel() {

    private var challenge: ReaderChallenge = getReaderChallengeUseCase()
    private var progress: ReaderChallengeProgress = createInitialReaderChallengeProgressUseCase()

    private val _state: MutableStateFlow<ReaderUiState> = MutableStateFlow(
        ReaderChallengeToReaderUiStateMapper.map(
            challenge = challenge,
            progress = progress,
        ),
    )
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    fun onReadingInteractionTick(
        elapsedTime: Duration,
        isFingerDown: Boolean,
        isFingerMoving: Boolean,
    ) {
        progress = updateReaderChallengeProgressUseCase(
            progress = progress,
            elapsedTime = elapsedTime,
            isFingerDown = isFingerDown,
            isFingerMoving = isFingerMoving,
        )

        updateState()
    }

    fun onNextPageClick() {
        if (!progress.canGoToNextPage) {
            return
        }

        val nextPageIndex = challenge.currentPageIndex + 1

        if (nextPageIndex >= challenge.pages.size) {
            return
        }

        challenge = challenge.copy(currentPageIndex = nextPageIndex)
        progress = createInitialReaderChallengeProgressUseCase()

        updateState()
    }

    private fun updateState() {
        _state.value = createUiState()
    }

    private fun createUiState(): ReaderUiState {
        return ReaderChallengeToReaderUiStateMapper.map(
            challenge = challenge,
            progress = progress,
        )
    }
}
