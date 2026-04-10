package com.shelfie.feature.books.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shelfie.feature.books.domain.usecase.GetFavoriteBooksUseCase
import com.shelfie.feature.books.domain.usecase.SearchBooksUseCase
import com.shelfie.feature.books.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private val DEBOUNCE_TIMEOUT = 400.milliseconds
private const val MIN_QUERY_LENGTH = 2

@OptIn(FlowPreview::class)
@HiltViewModel
class BooksViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoriteBooksUseCase: GetFavoriteBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState

    private var searchObserveJob: Job? = null
    private var favoritesObserveJob: Job? = null

    init {
        // Debounced auto-search: triggers when query changes and meets minimum length
        _uiState
            .map { it.query.trim() }
            .distinctUntilChanged()
            .debounce(DEBOUNCE_TIMEOUT)
            .filter { it.length >= MIN_QUERY_LENGTH }
            .onEach { query -> performSearch(query) }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: BooksUiEvent) {
        when (event) {
            is BooksUiEvent.QueryChanged -> {
                _uiState.update { it.copy(query = event.query) }
            }
            is BooksUiEvent.Search -> performSearch(_uiState.value.query.trim())
            is BooksUiEvent.ToggleFavorite -> {
                viewModelScope.launch { toggleFavoriteUseCase(event.book) }
            }
            is BooksUiEvent.ToggleShowFavorites -> toggleFavorites()
            is BooksUiEvent.ClearSearch -> {
                searchObserveJob?.cancel()
                _uiState.update {
                    it.copy(query = "", books = persistentListOf(), hasSearched = false, errorMessage = null)
                }
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return

        // Switch back to search results view if currently showing favorites
        _uiState.update { it.copy(isLoading = true, errorMessage = null, showFavorites = false) }

        searchObserveJob?.cancel()
        searchObserveJob = searchBooksUseCase.observe(query)
            .onEach { books ->
                _uiState.update { it.copy(books = books.toImmutableList(), isLoading = false, hasSearched = true) }
            }
            .catch { e ->
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false, hasSearched = true) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            try {
                searchBooksUseCase.refresh(query)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false, hasSearched = true) }
            }
        }
    }

    private fun toggleFavorites() {
        val entering = !_uiState.value.showFavorites
        _uiState.update { it.copy(showFavorites = entering) }

        if (entering) {
            searchObserveJob?.cancel()
            favoritesObserveJob?.cancel()
            favoritesObserveJob = getFavoriteBooksUseCase()
                .onEach { favorites -> _uiState.update { it.copy(books = favorites.toImmutableList()) } }
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                .launchIn(viewModelScope)
        } else {
            favoritesObserveJob?.cancel()
            // Re-run the last search if there was one
            val query = _uiState.value.query.trim()
            if (query.length >= MIN_QUERY_LENGTH) {
                performSearch(query)
            } else {
                _uiState.update { it.copy(books = persistentListOf()) }
            }
        }
    }
}
