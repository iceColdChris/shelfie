package com.shelfie.core.domain

import com.shelfie.core.model.ReadingListBook
import com.shelfie.core.model.Shelf
import com.shelfie.core.data.repository.ReadingListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes all books on a given shelf. */
class GetShelfBooksUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    operator fun invoke(shelf: Shelf): Flow<List<ReadingListBook>> =
        repository.observeByShelf(shelf)
}
