package com.shelfie.feature.books.domain.repository

import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.model.BookDetail
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for book search and favorite operations.
 * Implementations follow the offline-first pattern: the UI observes the local database,
 * and network results are synced into a temporary cache table.
 */
interface BookRepository {

    /**
     * Observes cached search results for the given query from the local database.
     * Triggers a network refresh in the background on first call.
     */
    fun observeSearchResults(query: String): Flow<List<Book>>

    /** Fetches books from the network and syncs them into the local cache. */
    suspend fun refreshSearch(query: String)

    /** Observes all books the user has marked as favorites. */
    fun observeFavorites(): Flow<List<Book>>

    /** Adds a book to the favorites table. */
    suspend fun addFavorite(book: Book)

    /** Removes a book from the favorites table by its id. */
    suspend fun removeFavorite(bookId: String)

    /** Looks up a book from the local cache or favorites table. */
    suspend fun getCachedBook(bookId: String): Book?

    /** Fetches extended detail from the Open Library Works endpoint. */
    suspend fun getBookDetail(bookId: String): BookDetail
}
