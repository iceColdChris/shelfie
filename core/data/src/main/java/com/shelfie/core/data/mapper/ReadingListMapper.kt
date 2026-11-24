package com.shelfie.core.data.mapper

import com.shelfie.core.database.entity.ReadingListEntry
import com.shelfie.core.model.ReadingListBook
import com.shelfie.core.model.Shelf
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

internal object ReadingListMapper {

    fun ReadingListEntry.toDomain(): ReadingListBook {
        return ReadingListBook(
            bookId = bookId,
            title = title,
            authors = authors.toImmutableList(),
            thumbnail = thumbnail,
            shelf = Shelf.fromValue(shelf),
            progress = progress,
            sortOrder = sortOrder,
            addedAt = Instant.fromEpochMilliseconds(addedAt),
            finishedAt = finishedAt?.let { Instant.fromEpochMilliseconds(it) }
        )
    }

    fun ReadingListBook.toEntity(): ReadingListEntry {
        return ReadingListEntry(
            bookId = bookId,
            title = title,
            authors = authors,
            thumbnail = thumbnail,
            shelf = shelf.value,
            progress = progress,
            sortOrder = sortOrder,
            addedAt = addedAt.toEpochMilliseconds(),
            finishedAt = finishedAt?.toEpochMilliseconds()
        )
    }
}
