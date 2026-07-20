package com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel

import androidx.lifecycle.ViewModel
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallenge
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderChallengeProgress
import com.ruslanataev.razbudilnik.domain.reader.usecases.CreateInitialReaderChallengeProgressUseCase
import com.ruslanataev.razbudilnik.domain.reader.usecases.GetReaderChallengeUseCase
import com.ruslanataev.razbudilnik.presentation.ui.reader.mappers.ReaderChallengeToReaderUiStateMapper
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.ReaderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getReaderChallengeUseCase: GetReaderChallengeUseCase,
    private val createInitialReaderChallengeProgressUseCase: CreateInitialReaderChallengeProgressUseCase,
) : ViewModel() {

    private val challenge: ReaderChallenge = getReaderChallengeUseCase()
    private val progress: ReaderChallengeProgress = createInitialReaderChallengeProgressUseCase()

    private val _state: MutableStateFlow<ReaderUiState> = MutableStateFlow(
        ReaderChallengeToReaderUiStateMapper.map(
            challenge = challenge,
            progress = progress,
        ),
    )
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()
}
