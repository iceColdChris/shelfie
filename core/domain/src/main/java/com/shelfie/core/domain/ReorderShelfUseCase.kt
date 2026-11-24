package com.shelfie.core.domain

import com.shelfie.core.data.repository.ReadingListRepository
import com.shelfie.core.model.ReadingListBook
import javax.inject.Inject

/** Persists the order of the given books, in list order, after a drag-to-reorder. */
class ReorderShelfUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    suspend operator fun invoke(books: List<ReadingListBook>) =
        repository.reorder(books)
}
