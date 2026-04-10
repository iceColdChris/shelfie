package com.shelfie.feature.readinglist.data.mapper

import com.shelfie.core.data.database.entity.ReadingListEntry
import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
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
