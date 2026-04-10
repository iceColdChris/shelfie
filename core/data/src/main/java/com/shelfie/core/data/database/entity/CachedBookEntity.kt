package com.shelfie.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Temporary cache entity for storing search results from the Open Library API.
 * This table is cleared and repopulated on each new search query.
 */
@Entity(
    tableName = "cached_books",
    indices = [Index("query")]
)
data class CachedBookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?,
    val query: String,
    val subjects: List<String> = emptyList()
)
