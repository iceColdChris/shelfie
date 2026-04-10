package com.shelfie.feature.stats.data.repository

import app.cash.turbine.test
import com.shelfie.core.data.database.dao.ReadingListDao
import com.shelfie.core.data.database.entity.ReadingListEntry
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class StatsRepositoryImplTest {

    private val dao: ReadingListDao = mockk()
    private val repository = StatsRepositoryImpl(dao)

    // ---- Streak calculation ----

    @Test
    fun `streak is 0 when no finished books`() = runTest {
        setupDao(finishedBooks = emptyList())

        repository.observeStats().test {
            assertEquals(0, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 for a single finished book`() = runTest {
        setupDao(finishedBooks = listOf(entry(finishedAt = daysAgo(0))))

        repository.observeStats().test {
            assertEquals(1, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 when two books finished on the same day`() = runTest {
        val sameDay = daysAgo(1)
        setupDao(finishedBooks = listOf(
            entry(finishedAt = sameDay),
            entry(finishedAt = sameDay + 1_000), // same day, 1 second apart
        ))

        repository.observeStats().test {
            assertEquals(1, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak counts consecutive days correctly`() = runTest {
        setupDao(finishedBooks = listOf(
            entry(finishedAt = daysAgo(0)),
            entry(finishedAt = daysAgo(1)),
            entry(finishedAt = daysAgo(2)),
        ))

        repository.observeStats().test {
            assertEquals(3, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak breaks on a gap day`() = runTest {
        // Books finished today and 2 days ago — day 1 missing, streak should be 1
        setupDao(finishedBooks = listOf(
            entry(finishedAt = daysAgo(0)),
            entry(finishedAt = daysAgo(2)),
        ))

        repository.observeStats().test {
            assertEquals(1, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak counts from most recent day and stops at gap`() = runTest {
        // 3 consecutive days, then a gap, then 2 more consecutive days
        setupDao(finishedBooks = listOf(
            entry(finishedAt = daysAgo(0)),
            entry(finishedAt = daysAgo(1)),
            entry(finishedAt = daysAgo(2)),
            // gap on day 3
            entry(finishedAt = daysAgo(4)),
            entry(finishedAt = daysAgo(5)),
        ))

        repository.observeStats().test {
            assertEquals(3, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- Monthly chart ----

    @Test
    fun `monthly chart always has exactly 6 entries`() = runTest {
        setupDao(finishedBooks = emptyList())

        repository.observeStats().test {
            assertEquals(6, awaitItem().monthlyFinished.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthly chart has all zeros when no books finished`() = runTest {
        setupDao(finishedBooks = emptyList())

        repository.observeStats().test {
            val counts = awaitItem().monthlyFinished.map { it.count }
            assertTrue(counts.all { it == 0 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthly chart counts books finished this month in the last slot`() = runTest {
        setupDao(finishedBooks = listOf(
            entry(finishedAt = daysAgo(0)),
            entry(finishedAt = daysAgo(3)),
        ))

        repository.observeStats().test {
            val chart = awaitItem().monthlyFinished
            // The last entry is the current month — both books are within the last few days
            // so they should both appear in the last slot (assuming same calendar month)
            assertEquals(6, chart.size)
            assertTrue("Current month should have at least 2 books", chart.last().count >= 2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthly chart does not count books older than 6 months`() = runTest {
        setupDao(finishedBooks = listOf(
            entry(finishedAt = daysAgo(220)), // ~7+ months ago
        ))

        repository.observeStats().test {
            val totalCount = awaitItem().monthlyFinished.sumOf { it.count }
            assertEquals(0, totalCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthly chart entries have valid abbreviated month labels`() = runTest {
        val validLabels = setOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        setupDao(finishedBooks = emptyList())

        repository.observeStats().test {
            val labels = awaitItem().monthlyFinished.map { it.label }
            assertTrue("All labels should be valid month abbreviations",
                labels.all { it in validLabels })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthly chart labels are in chronological order`() = runTest {
        setupDao(finishedBooks = emptyList())

        repository.observeStats().test {
            val chart = awaitItem().monthlyFinished
            // The last label should be the current month
            val validLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val indices = chart.map { validLabels.indexOf(it.label) }
            // Verify that month indices are in increasing order (accounting for year wrap)
            for (i in 0 until indices.size - 1) {
                val diff = (indices[i + 1] - indices[i] + 12) % 12
                assertEquals("Month labels should be 1 month apart", 1, diff)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- DAO aggregates are passed through ----

    @Test
    fun `observeStats passes total and currently reading counts from dao`() = runTest {
        every { dao.observeTotalCount() } returns flowOf(10)
        every { dao.observeCountByShelf("CURRENTLY_READING") } returns flowOf(3)
        every { dao.observeFinishedSince(any()) } returns flowOf(5)
        every { dao.observeFinishedBooks() } returns flowOf(emptyList())

        repository.observeStats().test {
            val stats = awaitItem()
            assertEquals(10, stats.totalBooks)
            assertEquals(3, stats.currentlyReading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- Helpers ----

    /**
     * Returns an epoch millisecond timestamp [n] whole days before the current moment.
     * Using 25-hour offsets ensures the dates are reliably on different calendar days
     * regardless of the time of day the test runs.
     */
    private fun daysAgo(n: Int): Long =
        (kotlinx.datetime.Clock.System.now() - (n * 25).hours).toEpochMilliseconds()

    private fun entry(finishedAt: Long?) = ReadingListEntry(
        bookId = "b${finishedAt}",
        title = "Book",
        authors = emptyList(),
        thumbnail = null,
        shelf = "FINISHED",
        progress = 100,
        sortOrder = 0,
        addedAt = finishedAt ?: 0L,
        finishedAt = finishedAt,
    )

    private fun setupDao(finishedBooks: List<ReadingListEntry>) {
        every { dao.observeTotalCount() } returns flowOf(finishedBooks.size)
        every { dao.observeCountByShelf(any()) } returns flowOf(0)
        every { dao.observeFinishedSince(any()) } returns flowOf(finishedBooks.size)
        every { dao.observeFinishedBooks() } returns flowOf(finishedBooks)
    }
}
