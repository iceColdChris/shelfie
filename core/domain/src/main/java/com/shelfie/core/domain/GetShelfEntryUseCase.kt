package com.shelfie.core.domain

import com.shelfie.core.data.repository.ReadingListRepository
import com.shelfie.core.model.ReadingListBook
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the reading list entry for a book; emits null while it is on no shelf. */
class GetShelfEntryUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    operator fun invoke(bookId: String): Flow<ReadingListBook?> =
        repository.observeEntry(bookId)
}
