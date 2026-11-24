package com.shelfie.core.domain

import com.shelfie.core.model.BookDetail
import com.shelfie.core.data.repository.BookRepository
import javax.inject.Inject

/**
 * Fetches extended book information from the Open Library Works endpoint.
 */
class GetBookDetailUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(bookId: String): BookDetail = repository.getBookDetail(bookId)
}
