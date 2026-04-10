package com.shelfie.feature.readinglist.domain.repository

import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for reading list operations.
 */
interface ReadingListRepository {

    /** Observes all books on the given shelf, ordered by sortOrder. */
    fun observeByShelf(shelf: Shelf): Flow<List<ReadingListBook>>

    /** Adds a book to the given shelf. */
    suspend fun addToShelf(
        bookId: String,
        title: String,
        authors: List<String>,
        thumbnail: String?,
        shelf: Shelf
    )

    /** Removes a book from the reading list entirely. */
    suspend fun remove(bookId: String)

    /** Updates reading progress (0-100) for a book. */
    suspend fun updateProgress(bookId: String, progress: Int)

    /** Moves a book to a different shelf. */
    suspend fun moveToShelf(bookId: String, shelf: Shelf)

    /** Persists a new sort order for books on a shelf after drag-to-reorder. */
    suspend fun reorder(books: List<ReadingListBook>)

    /** Observes the shelf entry for a specific book (null if not on any shelf). */
    fun observeEntry(bookId: String): Flow<ReadingListBook?>
}
