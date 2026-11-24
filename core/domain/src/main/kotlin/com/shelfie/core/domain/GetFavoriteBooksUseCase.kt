package com.shelfie.core.domain

import com.shelfie.core.model.Book
import com.shelfie.core.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes all books the user has saved as favorites.
 */
class GetFavoriteBooksUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(): Flow<List<Book>> = repository.observeFavorites()
}
