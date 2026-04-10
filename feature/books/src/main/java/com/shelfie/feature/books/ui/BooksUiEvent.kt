package com.shelfie.feature.books.ui

import com.shelfie.feature.books.domain.model.Book

/**
 * Sealed interface representing user-driven actions on the Books screen.
 */
sealed interface BooksUiEvent {
    data class QueryChanged(val query: String) : BooksUiEvent
    data object Search : BooksUiEvent
    data class ToggleFavorite(val book: Book) : BooksUiEvent
    data object ToggleShowFavorites : BooksUiEvent
    data object ClearSearch : BooksUiEvent
}
