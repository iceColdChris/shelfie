package com.shelfie.feature.readinglist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shelfie.feature.readinglist.domain.model.Shelf
import com.shelfie.feature.readinglist.domain.usecase.GetShelfBooksUseCase
import com.shelfie.feature.readinglist.domain.usecase.RemoveFromShelfUseCase
import com.shelfie.feature.readinglist.domain.usecase.UpdateProgressUseCase
import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingListViewModel @Inject constructor(
    private val getShelfBooksUseCase: GetShelfBooksUseCase,
    private val removeFromShelfUseCase: RemoveFromShelfUseCase,
    private val updateProgressUseCase: UpdateProgressUseCase,
    private val readingListRepository: ReadingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingListUiState())
    val uiState: StateFlow<ReadingListUiState> = _uiState

    private var observeJob: Job? = null

    init {
        observeShelf(Shelf.WANT_TO_READ)
    }

    fun onEvent(event: ReadingListUiEvent) {
        when (event) {
            is ReadingListUiEvent.SelectShelf -> {
                _uiState.update { it.copy(selectedShelf = event.shelf) }
                observeShelf(event.shelf)
            }
            is ReadingListUiEvent.UpdateProgress -> {
                viewModelScope.launch { updateProgressUseCase(event.bookId, event.progress) }
            }
            is ReadingListUiEvent.RemoveBook -> {
                viewModelScope.launch { removeFromShelfUseCase(event.bookId) }
            }
            is ReadingListUiEvent.MoveToShelf -> {
                viewModelScope.launch { readingListRepository.moveToShelf(event.bookId, event.shelf) }
            }
            is ReadingListUiEvent.Reorder -> {
                viewModelScope.launch { readingListRepository.reorder(event.books) }
            }
        }
    }

    private fun observeShelf(shelf: Shelf) {
        observeJob?.cancel()
        observeJob = getShelfBooksUseCase(shelf)
            .onEach { books ->
                _uiState.update { it.copy(books = books.toImmutableList()) }
            }
            .catch { /* Room errors are non-recoverable here; state remains unchanged */ }
            .launchIn(viewModelScope)
    }
}
