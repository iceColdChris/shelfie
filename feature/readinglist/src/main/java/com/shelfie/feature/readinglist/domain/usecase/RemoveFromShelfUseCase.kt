package com.shelfie.feature.readinglist.domain.usecase

import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import javax.inject.Inject

/** Removes a book from the reading list. */
class RemoveFromShelfUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    suspend operator fun invoke(bookId: String) = repository.remove(bookId)
}
