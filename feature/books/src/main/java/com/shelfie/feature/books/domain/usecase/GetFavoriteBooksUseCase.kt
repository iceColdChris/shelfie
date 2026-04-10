package com.shelfie.feature.books.domain.usecase

import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.repository.BookRepository
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
