package com.shelfie.feature.books.ui

import androidx.compose.runtime.Immutable
import com.shelfie.feature.books.domain.model.Book
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Immutable UI state for the Books screen.
 */
@Immutable
data class BooksUiState(
    val query: String = "",
    val books: ImmutableList<Book> = persistentListOf(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showFavorites: Boolean = false,
    val hasSearched: Boolean = false
)
