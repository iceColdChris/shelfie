package com.shelfie.feature.books.domain.usecase

import com.shelfie.feature.books.domain.model.BookDetail
import com.shelfie.feature.books.domain.repository.BookRepository
import javax.inject.Inject

/**
 * Fetches extended book information from the Open Library Works endpoint.
 */
class GetBookDetailUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(bookId: String): BookDetail = repository.getBookDetail(bookId)
}
