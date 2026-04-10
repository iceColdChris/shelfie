package com.shelfie.feature.books.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Extended domain model with information from the Open Library Works endpoint.
 * Used by the detail screen to display richer book information.
 */
@Immutable
data class BookDetail(
    val id: String,
    val title: String,
    val authors: ImmutableList<String>,
    val thumbnail: String?,
    val description: String?,
    val subjects: ImmutableList<String>,
    val isFavorite: Boolean = false
) {
    /** Converts this detail back to a [Book] for use with favorite-toggle logic. */
    fun toBook(): Book = Book(
        id = id,
        title = title,
        authors = authors,
        thumbnail = thumbnail,
        isFavorite = isFavorite
    )

    companion object {
        val EMPTY = BookDetail(
            id = "", title = "", authors = persistentListOf(),
            thumbnail = null, description = null,
            subjects = persistentListOf()
        )
    }
}
