package com.shelfie.feature.readinglist.domain.usecase

import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes all books on a given shelf. */
class GetShelfBooksUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    operator fun invoke(shelf: Shelf): Flow<List<ReadingListBook>> =
        repository.observeByShelf(shelf)
}
