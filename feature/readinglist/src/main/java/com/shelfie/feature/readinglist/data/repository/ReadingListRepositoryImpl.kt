package com.shelfie.feature.readinglist.data.repository

import com.shelfie.core.data.database.dao.ReadingListDao
import com.shelfie.core.data.database.entity.ReadingListEntry
import com.shelfie.feature.readinglist.data.mapper.ReadingListMapper.toDomain
import com.shelfie.feature.readinglist.data.mapper.ReadingListMapper.toEntity
import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Clock

internal class ReadingListRepositoryImpl @Inject constructor(
    private val dao: ReadingListDao
) : ReadingListRepository {

    override fun observeByShelf(shelf: Shelf): Flow<List<ReadingListBook>> {
        return dao.observeByShelf(shelf.value).map { entries ->
            entries.map { it.toDomain() }
        }
    }

    override suspend fun addToShelf(
        bookId: String,
        title: String,
        authors: List<String>,
        thumbnail: String?,
        shelf: Shelf
    ) {
        val nextOrder = dao.getMaxSortOrder(shelf.value) + 1
        val now = Clock.System.now().toEpochMilliseconds()
        val finishedAt = if (shelf == Shelf.FINISHED) now else null
        dao.upsert(
            ReadingListEntry(
                bookId = bookId,
                title = title,
                authors = authors,
                thumbnail = thumbnail,
                shelf = shelf.value,
                progress = if (shelf == Shelf.FINISHED) 100 else 0,
                sortOrder = nextOrder,
                addedAt = now,
                finishedAt = finishedAt
            )
        )
    }

    override suspend fun remove(bookId: String) {
        dao.remove(bookId)
    }

    override suspend fun updateProgress(bookId: String, progress: Int) {
        dao.updateProgress(bookId, progress)
    }

    override suspend fun moveToShelf(bookId: String, shelf: Shelf) {
        val finishedAt = if (shelf == Shelf.FINISHED) {
            Clock.System.now().toEpochMilliseconds()
        } else {
            null
        }
        dao.updateShelf(bookId, shelf.value, finishedAt)
        if (shelf == Shelf.FINISHED) {
            dao.updateProgress(bookId, 100)
        }
    }

    override suspend fun reorder(books: List<ReadingListBook>) {
        val reordered = books.mapIndexed { index, book ->
            book.copy(sortOrder = index).toEntity()
        }
        dao.updateSortOrders(reordered)
    }

    override fun observeEntry(bookId: String): Flow<ReadingListBook?> {
        return dao.observeEntry(bookId).map { it?.toDomain() }
    }
}
