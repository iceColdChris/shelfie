package com.shelfie.core.domain

import com.shelfie.core.model.Book
import com.shelfie.core.data.repository.BookRepository
import javax.inject.Inject

/**
 * Toggles the favorite status of a book. If the book is currently a favorite it is removed;
 * otherwise it is added.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(book: Book) {
        if (book.isFavorite) {
            repository.removeFavorite(book.id)
        } else {
            repository.addFavorite(book)
        }
    }
}
