package com.shelfie.feature.books.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Domain model representing a book. This is the single truth used by the UI layer.
 */
@Immutable
data class Book(
    val id: String,
    val title: String,
    val authors: ImmutableList<String>,
    val thumbnail: String?,
    val isFavorite: Boolean = false
) {
    companion object {
        val EMPTY = Book(id = "", title = "", authors = persistentListOf(), thumbnail = null)
    }
}
