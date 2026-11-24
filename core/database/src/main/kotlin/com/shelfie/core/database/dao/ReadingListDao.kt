package com.shelfie.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.shelfie.core.database.model.ReadingListEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for reading list operations.
 * Supports shelf management, progress tracking, and stats aggregation.
 */
@Dao
interface ReadingListDao {

    // -- Shelf queries --

    @Query("SELECT * FROM reading_list WHERE shelf = :shelf ORDER BY sortOrder ASC")
    fun observeByShelf(shelf: String): Flow<List<ReadingListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ReadingListEntity)

    @Query("DELETE FROM reading_list WHERE bookId = :bookId")
    suspend fun remove(bookId: String)

    @Query("SELECT * FROM reading_list WHERE bookId = :bookId LIMIT 1")
    suspend fun getEntry(bookId: String): ReadingListEntity?

    @Query("SELECT * FROM reading_list WHERE bookId = :bookId LIMIT 1")
    fun observeEntry(bookId: String): Flow<ReadingListEntity?>

    // -- Progress + shelf updates --

    @Query("UPDATE reading_list SET progress = :progress WHERE bookId = :bookId")
    suspend fun updateProgress(bookId: String, progress: Int)

    @Query("UPDATE reading_list SET shelf = :shelf, finishedAt = :finishedAt WHERE bookId = :bookId")
    suspend fun updateShelf(bookId: String, shelf: String, finishedAt: Long?)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM reading_list WHERE shelf = :shelf")
    suspend fun getMaxSortOrder(shelf: String): Int

    @Transaction
    suspend fun updateSortOrders(entries: List<ReadingListEntity>) {
        entries.forEach { upsert(it) }
    }

    // -- Stats queries --

    @Query("SELECT COUNT(*) FROM reading_list WHERE shelf = :shelf")
    fun observeCountByShelf(shelf: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM reading_list")
    fun observeTotalCount(): Flow<Int>

    @Query(
        "SELECT * FROM reading_list WHERE shelf = 'FINISHED' AND finishedAt IS NOT NULL " +
            "ORDER BY finishedAt DESC"
    )
    fun observeFinishedBooks(): Flow<List<ReadingListEntity>>

    @Query(
        "SELECT COUNT(*) FROM reading_list WHERE shelf = 'FINISHED' " +
            "AND finishedAt >= :sinceEpochMillis"
    )
    fun observeFinishedSince(sinceEpochMillis: Long): Flow<Int>
}
