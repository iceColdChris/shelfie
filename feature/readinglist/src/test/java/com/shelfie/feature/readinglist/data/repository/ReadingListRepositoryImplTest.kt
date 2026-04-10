package com.shelfie.feature.readinglist.data.repository

import app.cash.turbine.test
import com.shelfie.core.data.database.dao.ReadingListDao
import com.shelfie.core.data.database.entity.ReadingListEntry
import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingListRepositoryImplTest {

    private val dao: ReadingListDao = mockk()
    private val repository = ReadingListRepositoryImpl(dao)

    // ---- addToShelf ----

    @Test
    fun `addToShelf FINISHED sets progress to 100 and non-null finishedAt`() = runTest {
        coEvery { dao.getMaxSortOrder("FINISHED") } returns 0
        coEvery { dao.upsert(any()) } returns Unit

        repository.addToShelf("b1", "Dune", listOf("F. Herbert"), null, Shelf.FINISHED)

        coVerify {
            dao.upsert(match { entry ->
                entry.progress == 100 && entry.finishedAt != null && entry.shelf == "FINISHED"
            })
        }
    }

    @Test
    fun `addToShelf non-FINISHED sets progress to 0 and null finishedAt`() = runTest {
        coEvery { dao.getMaxSortOrder("WANT_TO_READ") } returns 2
        coEvery { dao.upsert(any()) } returns Unit

        repository.addToShelf("b1", "Dune", listOf("F. Herbert"), null, Shelf.WANT_TO_READ)

        coVerify {
            dao.upsert(match { entry ->
                entry.progress == 0 && entry.finishedAt == null && entry.shelf == "WANT_TO_READ"
            })
        }
    }

    @Test
    fun `addToShelf assigns sortOrder as max plus one`() = runTest {
        coEvery { dao.getMaxSortOrder("CURRENTLY_READING") } returns 4
        coEvery { dao.upsert(any()) } returns Unit

        repository.addToShelf("b1", "Dune", emptyList(), null, Shelf.CURRENTLY_READING)

        coVerify { dao.upsert(match { it.sortOrder == 5 }) }
    }

    // ---- moveToShelf ----

    @Test
    fun `moveToShelf FINISHED updates shelf and sets progress to 100`() = runTest {
        coEvery { dao.updateShelf(any(), any(), any()) } returns Unit
        coEvery { dao.updateProgress(any(), any()) } returns Unit

        repository.moveToShelf("b1", Shelf.FINISHED)

        coVerify { dao.updateShelf("b1", "FINISHED", match { it != null }) }
        coVerify { dao.updateProgress("b1", 100) }
    }

    @Test
    fun `moveToShelf non-FINISHED updates shelf with null finishedAt and does not update progress`() = runTest {
        coEvery { dao.updateShelf(any(), any(), any()) } returns Unit

        repository.moveToShelf("b1", Shelf.CURRENTLY_READING)

        coVerify { dao.updateShelf("b1", "CURRENTLY_READING", null) }
        coVerify(exactly = 0) { dao.updateProgress(any(), any()) }
    }

    // ---- remove ----

    @Test
    fun `remove delegates to dao`() = runTest {
        coEvery { dao.remove("b1") } returns Unit

        repository.remove("b1")

        coVerify(exactly = 1) { dao.remove("b1") }
    }

    // ---- updateProgress ----

    @Test
    fun `updateProgress delegates to dao`() = runTest {
        coEvery { dao.updateProgress(any(), any()) } returns Unit

        repository.updateProgress("b1", 75)

        coVerify { dao.updateProgress("b1", 75) }
    }

    // ---- reorder ----

    @Test
    fun `reorder reassigns sortOrder by list index`() = runTest {
        val books = listOf(
            book(bookId = "a", sortOrder = 99),
            book(bookId = "b", sortOrder = 0),
            book(bookId = "c", sortOrder = 5),
        )
        coEvery { dao.updateSortOrders(any()) } returns Unit

        repository.reorder(books)

        coVerify {
            dao.updateSortOrders(match { entries ->
                entries[0].sortOrder == 0 && entries[0].bookId == "a" &&
                entries[1].sortOrder == 1 && entries[1].bookId == "b" &&
                entries[2].sortOrder == 2 && entries[2].bookId == "c"
            })
        }
    }

    // ---- observeByShelf ----

    @Test
    fun `observeByShelf maps entries to domain models`() = runTest {
        val entries = listOf(entry(bookId = "b1", shelf = "WANT_TO_READ"))
        every { dao.observeByShelf("WANT_TO_READ") } returns flowOf(entries)

        repository.observeByShelf(Shelf.WANT_TO_READ).test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("b1", books[0].bookId)
            assertEquals(Shelf.WANT_TO_READ, books[0].shelf)
            awaitComplete()
        }
    }

    @Test
    fun `observeByShelf emits empty list when dao returns empty`() = runTest {
        every { dao.observeByShelf("FINISHED") } returns flowOf(emptyList())

        repository.observeByShelf(Shelf.FINISHED).test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    // ---- observeEntry ----

    @Test
    fun `observeEntry maps non-null entry to domain`() = runTest {
        every { dao.observeEntry("b1") } returns flowOf(entry(bookId = "b1"))

        repository.observeEntry("b1").test {
            assertNotNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeEntry maps null entry to null`() = runTest {
        every { dao.observeEntry("b1") } returns flowOf(null)

        repository.observeEntry("b1").test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    // ---- Helpers ----

    private fun entry(
        bookId: String = "b1",
        shelf: String = "WANT_TO_READ",
        progress: Int = 0,
        sortOrder: Int = 0,
    ) = ReadingListEntry(
        bookId = bookId,
        title = "Dune",
        authors = listOf("F. Herbert"),
        thumbnail = null,
        shelf = shelf,
        progress = progress,
        sortOrder = sortOrder,
        addedAt = 1_700_000_000_000L,
        finishedAt = null,
    )

    private fun book(
        bookId: String = "b1",
        sortOrder: Int = 0,
    ) = ReadingListBook(
        bookId = bookId,
        title = "Dune",
        authors = persistentListOf("F. Herbert"),
        thumbnail = null,
        shelf = Shelf.WANT_TO_READ,
        progress = 0,
        sortOrder = sortOrder,
        addedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        finishedAt = null,
    )
}
