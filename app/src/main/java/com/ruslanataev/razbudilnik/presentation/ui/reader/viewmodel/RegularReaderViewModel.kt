package com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBookIds
import com.ruslanataev.razbudilnik.domain.reader.usecases.GetReaderBookUseCase
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.RegularReaderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegularReaderViewModel @Inject constructor(
    private val getReaderBookUseCase: GetReaderBookUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<RegularReaderUiState?>(null)
    val state: StateFlow<RegularReaderUiState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadBook()
        }
    }

    private suspend fun loadBook() {
        val book = getReaderBookUseCase.invoke(ReaderBookIds.CAUCASIAN_PRISONER)

        _state.value = RegularReaderUiState(
            bookId = book.id,
            bookTitle = book.title,
            bookAuthor = book.author,
            bookText = book.text,
        )
    }
}
