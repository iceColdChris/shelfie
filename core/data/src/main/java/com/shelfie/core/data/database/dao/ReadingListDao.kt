package com.shelfie.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shelfie.core.data.database.entity.ReadingListEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for reading list operations.
 * Supports shelf management, progress tracking, and stats aggregation.
 */
@Dao
interface ReadingListDao {

    // -- Shelf queries --

    @Query("SELECT * FROM reading_list WHERE shelf = :shelf ORDER BY sortOrder ASC")
    fun observeByShelf(shelf: String): Flow<List<ReadingListEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ReadingListEntry)

    @Query("DELETE FROM reading_list WHERE bookId = :bookId")
    suspend fun remove(bookId: String)

    @Query("SELECT * FROM reading_list WHERE bookId = :bookId LIMIT 1")
    suspend fun getEntry(bookId: String): ReadingListEntry?

    @Query("SELECT * FROM reading_list WHERE bookId = :bookId LIMIT 1")
    fun observeEntry(bookId: String): Flow<ReadingListEntry?>

    // -- Progress + shelf updates --

    @Query("UPDATE reading_list SET progress = :progress WHERE bookId = :bookId")
    suspend fun updateProgress(bookId: String, progress: Int)

    @Query("UPDATE reading_list SET shelf = :shelf, finishedAt = :finishedAt WHERE bookId = :bookId")
    suspend fun updateShelf(bookId: String, shelf: String, finishedAt: Long?)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM reading_list WHERE shelf = :shelf")
    suspend fun getMaxSortOrder(shelf: String): Int

    @Transaction
    suspend fun updateSortOrders(entries: List<ReadingListEntry>) {
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
    fun observeFinishedBooks(): Flow<List<ReadingListEntry>>

    @Query(
        "SELECT COUNT(*) FROM reading_list WHERE shelf = 'FINISHED' " +
            "AND finishedAt >= :sinceEpochMillis"
    )
    fun observeFinishedSince(sinceEpochMillis: Long): Flow<Int>
}
