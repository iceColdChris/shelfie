package com.shelfie.feature.books.ui

import androidx.compose.runtime.Immutable
import com.shelfie.feature.books.domain.model.BookDetail

/**
 * Immutable UI state for the Book Detail screen.
 */
@Immutable
data class BookDetailUiState(
    val detail: BookDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentShelf: String? = null,
    val showShelfPicker: Boolean = false
)
