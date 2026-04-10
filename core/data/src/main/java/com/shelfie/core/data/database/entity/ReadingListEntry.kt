package com.shelfie.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistent entity representing a book on the user's reading list.
 * Fields are denormalized (title, authors, thumbnail) so the reading list can be
 * displayed without joins or network calls.
 */
@Entity(
    tableName = "reading_list",
    indices = [Index("shelf")]
)
data class ReadingListEntry(
    @PrimaryKey val bookId: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?,
    val shelf: String,
    val progress: Int = 0,
    val sortOrder: Int = 0,
    val addedAt: Long,
    val finishedAt: Long? = null
)
