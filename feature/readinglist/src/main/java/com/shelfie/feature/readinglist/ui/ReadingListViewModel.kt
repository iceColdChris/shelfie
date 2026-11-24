package com.shelfie.feature.readinglist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shelfie.core.model.Shelf
import com.shelfie.core.domain.MoveToShelfUseCase
import com.shelfie.core.domain.ReorderShelfUseCase
import com.shelfie.core.domain.GetShelfBooksUseCase
import com.shelfie.core.domain.RemoveFromShelfUseCase
import com.shelfie.core.domain.UpdateProgressUseCase
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
    private val moveToShelfUseCase: MoveToShelfUseCase,
    private val reorderShelfUseCase: ReorderShelfUseCase
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
                viewModelScope.launch { moveToShelfUseCase(event.bookId, event.shelf) }
            }
            is ReadingListUiEvent.Reorder -> {
                viewModelScope.launch { reorderShelfUseCase(event.books) }
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
