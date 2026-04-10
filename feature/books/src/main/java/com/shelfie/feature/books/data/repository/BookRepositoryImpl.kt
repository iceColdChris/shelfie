package com.shelfie.feature.books.data.repository

import com.shelfie.core.data.database.dao.BookDao
import com.shelfie.feature.books.data.mapper.BookMapper.toBookDetail
import com.shelfie.feature.books.data.mapper.BookMapper.toCachedEntity
import com.shelfie.feature.books.data.mapper.BookMapper.toDomain
import com.shelfie.feature.books.data.mapper.BookMapper.toFavoriteEntity
import com.shelfie.feature.books.data.remote.OpenLibraryApi
import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.model.BookDetail
import com.shelfie.feature.books.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first implementation. The UI observes the Room cache table, and
 * [refreshSearch] syncs network results into it.
 */
internal class BookRepositoryImpl @Inject constructor(
    private val api: OpenLibraryApi,
    private val bookDao: BookDao
) : BookRepository {

    override fun observeSearchResults(query: String): Flow<List<Book>> {
        return combine(
            bookDao.observeCachedBooks(query),
            bookDao.observeFavoriteBooks()
        ) { cached, favorites ->
            val favoriteIds = favorites.map { it.id }.toSet()
            cached.map { entity -> entity.toDomain(isFavorite = entity.id in favoriteIds) }
        }
    }

    override suspend fun refreshSearch(query: String) {
        val response = api.searchBooks(query)
        val entities = response.docs?.map { dto -> dto.toCachedEntity(query) } ?: emptyList()
        bookDao.replaceCacheForQuery(query, entities)
    }

    override fun observeFavorites(): Flow<List<Book>> {
        return bookDao.observeFavoriteBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addFavorite(book: Book) {
        bookDao.insertFavorite(book.toFavoriteEntity())
    }

    override suspend fun removeFavorite(bookId: String) {
        bookDao.removeFavorite(bookId)
    }

    override suspend fun getCachedBook(bookId: String): Book? {
        val favorite = bookDao.getFavoriteBookById(bookId)
        val cached = bookDao.getCachedBookById(bookId)
        if (cached != null) return cached.toDomain(isFavorite = favorite != null)
        return favorite?.toDomain()
    }

    override suspend fun getBookDetail(bookId: String): BookDetail {
        val cachedEntity = bookDao.getCachedBookById(bookId)
        val favorite = bookDao.getFavoriteBookById(bookId)
        val book = when {
            cachedEntity != null -> cachedEntity.toDomain(isFavorite = favorite != null)
            favorite != null -> favorite.toDomain()
            else -> Book.EMPTY.copy(id = bookId)
        }
        val cachedSubjects = cachedEntity?.subjects ?: emptyList()
        val workId = bookId.substringAfterLast("/")
        val detail = api.getWorkDetail(workId)
        return detail.toBookDetail(book, cachedSubjects)
    }
}
