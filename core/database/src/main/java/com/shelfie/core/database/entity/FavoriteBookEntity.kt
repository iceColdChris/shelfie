package com.shelfie.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

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
