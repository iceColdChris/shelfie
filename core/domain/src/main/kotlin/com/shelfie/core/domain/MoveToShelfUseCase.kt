package com.shelfie.core.domain

import com.shelfie.core.data.repository.ReadingListRepository
import com.shelfie.core.model.Shelf
import javax.inject.Inject

/** Moves a book already on the reading list to a different shelf. */
class MoveToShelfUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    suspend operator fun invoke(bookId: String, shelf: Shelf) =
        repository.moveToShelf(bookId, shelf)
}
