package com.shelfie.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shelfie.core.data.database.entity.CachedBookEntity
import com.shelfie.core.data.database.entity.FavoriteBookEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for book-related database operations.
 * Supports both the temporary search cache and the persistent favorites table.
 */
@Dao
interface BookDao {

    // -- Cached books (search results) --

    @Query("SELECT * FROM cached_books WHERE `query` = :query")
    fun observeCachedBooks(query: String): Flow<List<CachedBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedBooks(books: List<CachedBookEntity>)

    @Query("DELETE FROM cached_books WHERE `query` = :query")
    suspend fun clearCacheForQuery(query: String)

    /** Atomically replaces cached search results for a given query. */
    @Transaction
    suspend fun replaceCacheForQuery(query: String, books: List<CachedBookEntity>) {
        clearCacheForQuery(query)
        insertCachedBooks(books)
    }

    @Query("DELETE FROM cached_books")
    suspend fun clearAllCache()

    // -- Favorite books --

    @Query("SELECT * FROM favorite_books")
    fun observeFavoriteBooks(): Flow<List<FavoriteBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(book: FavoriteBookEntity)

    @Query("DELETE FROM favorite_books WHERE id = :bookId")
    suspend fun removeFavorite(bookId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE id = :bookId)")
    fun isFavorite(bookId: String): Flow<Boolean>

    @Query("SELECT * FROM cached_books WHERE id = :bookId LIMIT 1")
    suspend fun getCachedBookById(bookId: String): CachedBookEntity?

    @Query("SELECT * FROM favorite_books WHERE id = :bookId LIMIT 1")
    suspend fun getFavoriteBookById(bookId: String): FavoriteBookEntity?
}
