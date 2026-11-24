package com.shelfie.core.data.repository

import app.cash.turbine.test
import com.shelfie.core.database.dao.ReadingListDao
import com.shelfie.core.database.model.ReadingListEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class DefaultStatsRepositoryTest {

    private val dao: ReadingListDao = mockk()
    private val repository = DefaultStatsRepository(dao)

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
            entry(finishedAt = firstOfThisMonth()),
        ))

        repository.observeStats().test {
            val chart = awaitItem().monthlyFinished
            // Today and the 1st are always in the current month, the last slot.
            assertEquals(6, chart.size)
            assertEquals(2, chart.last().count)
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

    // Timestamps are built from calendar dates in the system zone, as the repository reads them.
    // Fixed-hour offsets drift across midnight (25h * 2 = 50h lands 3 days back after 00:00-02:00).

    /** Noon, local time, [n] calendar days before today. Noon keeps clear of DST transitions. */
    private fun daysAgo(n: Int): Long = atNoon(today().minus(n, DateTimeUnit.DAY))

    /** Noon on the 1st of the current month. */
    private fun firstOfThisMonth(): Long = today().let { atNoon(LocalDate(it.year, it.month, 1)) }

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun atNoon(date: LocalDate): Long =
        date.atTime(12, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    private fun entry(finishedAt: Long?) = ReadingListEntity(
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

    private fun setupDao(finishedBooks: List<ReadingListEntity>) {
        every { dao.observeTotalCount() } returns flowOf(finishedBooks.size)
        every { dao.observeCountByShelf(any()) } returns flowOf(0)
        every { dao.observeFinishedSince(any()) } returns flowOf(finishedBooks.size)
        every { dao.observeFinishedBooks() } returns flowOf(finishedBooks)
    }
}
