package com.shelfie.feature.books.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shelfie.core.data.database.dao.ReadingListDao
import com.shelfie.core.data.database.entity.ReadingListEntry
import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.usecase.GetBookDetailUseCase
import com.shelfie.feature.books.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val readingListDao: ReadingListDao
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

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
            val now = Clock.System.now().toEpochMilliseconds()
            val nextOrder = readingListDao.getMaxSortOrder(shelf) + 1
            readingListDao.upsert(
                ReadingListEntry(
                    bookId = detail.id,
                    title = detail.title,
                    authors = detail.authors,
                    thumbnail = detail.thumbnail,
                    shelf = shelf,
                    progress = if (shelf == "FINISHED") 100 else 0,
                    sortOrder = nextOrder,
                    addedAt = now,
                    finishedAt = if (shelf == "FINISHED") now else null
                )
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
            }
        }
    }

    private fun observeShelfStatus() {
        readingListDao.observeEntry(bookId)
            .onEach { entry ->
                _uiState.update { it.copy(currentShelf = entry?.shelf) }
            }
            .launchIn(viewModelScope)
    }
}
