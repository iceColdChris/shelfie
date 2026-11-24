package com.shelfie.feature.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shelfie.core.domain.AddToShelfUseCase
import com.shelfie.core.domain.GetBookDetailUseCase
import com.shelfie.core.domain.GetShelfEntryUseCase
import com.shelfie.core.domain.ToggleFavoriteUseCase
import com.shelfie.core.model.Shelf
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = BookDetailViewModel.Factory::class)
class BookDetailViewModel @AssistedInject constructor(
    @Assisted private val bookId: String,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addToShelfUseCase: AddToShelfUseCase,
    private val getShelfEntryUseCase: GetShelfEntryUseCase
) : ViewModel() {

    // The navigation layer supplies bookId from the route, so it is typed and cannot be missing.
    @AssistedFactory
    interface Factory {
        fun create(bookId: String): BookDetailViewModel
    }

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState

    init {
        loadDetail()
        observeShelfStatus()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadDetail()
    }

    fun toggleFavorite() {
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(detail.toBook())
            _uiState.update {
                it.copy(detail = detail.copy(isFavorite = !detail.isFavorite))
            }
        }
    }

    fun showShelfPicker() {
        _uiState.update { it.copy(showShelfPicker = true) }
    }

    fun dismissShelfPicker() {
        _uiState.update { it.copy(showShelfPicker = false) }
    }

    fun addToShelf(shelf: String) {
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            addToShelfUseCase(
                bookId = detail.id,
                title = detail.title,
                authors = detail.authors,
                thumbnail = detail.thumbnail,
                shelf = Shelf.fromValue(shelf)
            )
            _uiState.update { it.copy(showShelfPicker = false) }
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            try {
                val detail = getBookDetailUseCase(bookId)
                _uiState.update {
                    it.copy(detail = detail, isLoading = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
            }
        }
    }

    private fun observeShelfStatus() {
        getShelfEntryUseCase(bookId)
            .onEach { entry ->
                _uiState.update { it.copy(currentShelf = entry?.shelf?.value) }
            }
            .launchIn(viewModelScope)
    }
}
