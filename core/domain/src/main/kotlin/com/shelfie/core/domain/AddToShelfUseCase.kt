package com.shelfie.core.domain

import com.shelfie.core.model.Shelf
import com.shelfie.core.data.repository.ReadingListRepository
import javax.inject.Inject

/** Adds a book to a shelf on the reading list. */
class AddToShelfUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    suspend operator fun invoke(
        bookId: String,
        title: String,
        authors: List<String>,
        thumbnail: String?,
        shelf: Shelf
    ) = repository.addToShelf(bookId, title, authors, thumbnail, shelf)
}
