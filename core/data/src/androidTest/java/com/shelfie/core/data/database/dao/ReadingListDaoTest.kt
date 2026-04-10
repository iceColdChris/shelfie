package com.shelfie.core.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.shelfie.core.data.database.AppDatabase
import com.shelfie.core.data.database.entity.ReadingListEntry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingListDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ReadingListDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.readingListDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---- observeByShelf ----

    @Test
    fun observeByShelf_returnsOnlyBooksOnThatShelf() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))
        dao.upsert(entry(bookId = "b2", shelf = "FINISHED"))

        dao.observeByShelf("WANT_TO_READ").test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("b1", books[0].bookId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeByShelf_emitsEmptyListWhenShelfIsEmpty() = runTest {
        dao.observeByShelf("WANT_TO_READ").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeByShelf_ordersResultsBySortOrderAscending() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ", sortOrder = 2))
        dao.upsert(entry(bookId = "b2", shelf = "WANT_TO_READ", sortOrder = 0))
        dao.upsert(entry(bookId = "b3", shelf = "WANT_TO_READ", sortOrder = 1))

        dao.observeByShelf("WANT_TO_READ").test {
            val books = awaitItem()
            assertEquals(listOf("b2", "b3", "b1"), books.map { it.bookId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeByShelf_emitsUpdatesReactively() = runTest {
        dao.observeByShelf("WANT_TO_READ").test {
            assertTrue(awaitItem().isEmpty())

            dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))
            assertEquals(1, awaitItem().size)

            dao.remove("b1")
            assertTrue(awaitItem().isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- remove ----

    @Test
    fun remove_deletesEntryFromShelf() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))
        dao.remove("b1")

        dao.observeByShelf("WANT_TO_READ").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- getEntry / observeEntry ----

    @Test
    fun getEntry_returnsCorrectEntry() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "CURRENTLY_READING"))

        val result = dao.getEntry("b1")
        assertNotNull(result)
        assertEquals("b1", result!!.bookId)
        assertEquals("CURRENTLY_READING", result.shelf)
    }

    @Test
    fun getEntry_returnsNullWhenNotFound() = runTest {
        assertNull(dao.getEntry("nonexistent"))
    }

    @Test
    fun observeEntry_emitsNullInitiallyThenEntryOnInsert() = runTest {
        dao.observeEntry("b1").test {
            assertNull(awaitItem())

            dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))
            assertNotNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeEntry_emitsNullAfterRemove() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))

        dao.observeEntry("b1").test {
            assertNotNull(awaitItem())

            dao.remove("b1")
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- updateProgress ----

    @Test
    fun updateProgress_updatesProgressForBook() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "CURRENTLY_READING", progress = 0))
        dao.updateProgress("b1", 75)

        assertEquals(75, dao.getEntry("b1")!!.progress)
    }

    @Test
    fun updateProgress_doesNotAffectOtherBooks() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "CURRENTLY_READING", progress = 10))
        dao.upsert(entry(bookId = "b2", shelf = "CURRENTLY_READING", progress = 20))

        dao.updateProgress("b1", 50)

        assertEquals(50, dao.getEntry("b1")!!.progress)
        assertEquals(20, dao.getEntry("b2")!!.progress)
    }

    // ---- updateShelf ----

    @Test
    fun updateShelf_changesShelfAndSetsFinishedAt() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "CURRENTLY_READING", finishedAt = null))
        dao.updateShelf("b1", "FINISHED", 1_710_000_000_000L)

        val updated = dao.getEntry("b1")!!
        assertEquals("FINISHED", updated.shelf)
        assertEquals(1_710_000_000_000L, updated.finishedAt)
    }

    @Test
    fun updateShelf_clearsFinishedAtWhenMovingOffFinished() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "FINISHED", finishedAt = 1_710_000_000_000L))
        dao.updateShelf("b1", "CURRENTLY_READING", null)

        val updated = dao.getEntry("b1")!!
        assertEquals("CURRENTLY_READING", updated.shelf)
        assertNull(updated.finishedAt)
    }

    // ---- getMaxSortOrder ----

    @Test
    fun getMaxSortOrder_returnsMinus1WhenShelfIsEmpty() = runTest {
        assertEquals(-1, dao.getMaxSortOrder("WANT_TO_READ"))
    }

    @Test
    fun getMaxSortOrder_returnsHighestSortOrderOnShelf() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ", sortOrder = 3))
        dao.upsert(entry(bookId = "b2", shelf = "WANT_TO_READ", sortOrder = 7))
        dao.upsert(entry(bookId = "b3", shelf = "WANT_TO_READ", sortOrder = 1))

        assertEquals(7, dao.getMaxSortOrder("WANT_TO_READ"))
    }

    @Test
    fun getMaxSortOrder_isIsolatedToGivenShelf() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "FINISHED", sortOrder = 99))

        assertEquals(-1, dao.getMaxSortOrder("WANT_TO_READ"))
    }

    // ---- updateSortOrders ----

    @Test
    fun updateSortOrders_updatesSortOrderForAllGivenEntries() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ", sortOrder = 0))
        dao.upsert(entry(bookId = "b2", shelf = "WANT_TO_READ", sortOrder = 1))

        dao.updateSortOrders(listOf(
            entry(bookId = "b2", shelf = "WANT_TO_READ", sortOrder = 0),
            entry(bookId = "b1", shelf = "WANT_TO_READ", sortOrder = 1),
        ))

        assertEquals(1, dao.getEntry("b1")!!.sortOrder)
        assertEquals(0, dao.getEntry("b2")!!.sortOrder)
    }

    // ---- observeCountByShelf ----

    @Test
    fun observeCountByShelf_returnsCountForGivenShelf() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))
        dao.upsert(entry(bookId = "b2", shelf = "WANT_TO_READ"))
        dao.upsert(entry(bookId = "b3", shelf = "FINISHED"))

        dao.observeCountByShelf("WANT_TO_READ").test {
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeCountByShelf_updatesReactively() = runTest {
        dao.observeCountByShelf("WANT_TO_READ").test {
            assertEquals(0, awaitItem())

            dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))
            assertEquals(1, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- observeTotalCount ----

    @Test
    fun observeTotalCount_countsAllEntriesAcrossAllShelves() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ"))
        dao.upsert(entry(bookId = "b2", shelf = "CURRENTLY_READING"))
        dao.upsert(entry(bookId = "b3", shelf = "FINISHED"))

        dao.observeTotalCount().test {
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- observeFinishedBooks ----

    @Test
    fun observeFinishedBooks_returnsOnlyFinishedEntriesWithNonNullFinishedAt() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "FINISHED", finishedAt = 1_000L))
        dao.upsert(entry(bookId = "b2", shelf = "FINISHED", finishedAt = null)) // excluded: null finishedAt
        dao.upsert(entry(bookId = "b3", shelf = "WANT_TO_READ", finishedAt = null)) // excluded: wrong shelf

        dao.observeFinishedBooks().test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("b1", books[0].bookId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFinishedBooks_orderedByFinishedAtDescending() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "FINISHED", finishedAt = 1_000L))
        dao.upsert(entry(bookId = "b2", shelf = "FINISHED", finishedAt = 3_000L))
        dao.upsert(entry(bookId = "b3", shelf = "FINISHED", finishedAt = 2_000L))

        dao.observeFinishedBooks().test {
            val ids = awaitItem().map { it.bookId }
            assertEquals(listOf("b2", "b3", "b1"), ids)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- observeFinishedSince ----

    @Test
    fun observeFinishedSince_countsOnlyBooksFinishedAtOrAfterThreshold() = runTest {
        val threshold = 2_000L
        dao.upsert(entry(bookId = "b1", shelf = "FINISHED", finishedAt = 1_000L)) // before threshold
        dao.upsert(entry(bookId = "b2", shelf = "FINISHED", finishedAt = 2_000L)) // at threshold
        dao.upsert(entry(bookId = "b3", shelf = "FINISHED", finishedAt = 3_000L)) // after threshold

        dao.observeFinishedSince(threshold).test {
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFinishedSince_returnsZeroWhenNoBooksFinishedAfterThreshold() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "FINISHED", finishedAt = 1_000L))

        dao.observeFinishedSince(5_000L).test {
            assertEquals(0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- upsert conflict resolution ----

    @Test
    fun upsert_replacesExistingEntryWithSameBookId() = runTest {
        dao.upsert(entry(bookId = "b1", shelf = "WANT_TO_READ", progress = 0))
        dao.upsert(entry(bookId = "b1", shelf = "CURRENTLY_READING", progress = 25))

        val entry = dao.getEntry("b1")!!
        assertEquals("CURRENTLY_READING", entry.shelf)
        assertEquals(25, entry.progress)
    }

    // ---- Helpers ----

    private fun entry(
        bookId: String,
        shelf: String,
        sortOrder: Int = 0,
        progress: Int = 0,
        finishedAt: Long? = null,
    ) = ReadingListEntry(
        bookId = bookId,
        title = "Book $bookId",
        authors = listOf("An Author"),
        thumbnail = null,
        shelf = shelf,
        progress = progress,
        sortOrder = sortOrder,
        addedAt = 1_700_000_000_000L,
        finishedAt = finishedAt,
    )
}
