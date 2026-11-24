package com.shelfie.core.domain

import com.shelfie.core.model.Book
import com.shelfie.core.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes locally cached search results and triggers a network refresh.
 */
class SearchBooksUseCase @Inject constructor(
    private val repository: BookRepository
) {

    /** Returns a Flow of cached results that updates after the network sync completes. */
    fun observe(query: String): Flow<List<Book>> = repository.observeSearchResults(query)

    /** Fetches fresh results from the network and writes them to the local cache. */
    suspend fun refresh(query: String) = repository.refreshSearch(query)
}
