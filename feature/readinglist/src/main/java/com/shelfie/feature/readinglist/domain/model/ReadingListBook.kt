package com.shelfie.feature.readinglist.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.Instant

/**
 * Domain model representing a book on the user's reading list.
 */
@Immutable
data class ReadingListBook(
    val bookId: String,
    val title: String,
    val authors: ImmutableList<String>,
    val thumbnail: String?,
    val shelf: Shelf,
    val progress: Int,
    val sortOrder: Int,
    val addedAt: Instant,
    val finishedAt: Instant?
)
