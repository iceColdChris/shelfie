package com.shelfie.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent entity representing a book the user has marked as a favorite.
 */
@Entity(tableName = "favorite_books")
data class FavoriteBookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?
)
