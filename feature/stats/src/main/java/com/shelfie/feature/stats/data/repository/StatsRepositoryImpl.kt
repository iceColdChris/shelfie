package com.shelfie.feature.stats.data.repository

import com.shelfie.core.data.database.dao.ReadingListDao
import com.shelfie.feature.stats.domain.model.MonthlyCount
import com.shelfie.feature.stats.domain.model.ReadingStats
import com.shelfie.feature.stats.domain.repository.StatsRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

internal class StatsRepositoryImpl @Inject constructor(
    private val dao: ReadingListDao
) : StatsRepository {

    override fun observeStats(): Flow<ReadingStats> {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz)

        val startOfMonth = LocalDate(today.year, today.monthNumber, 1).atStartOfDayIn(tz)
        val startOfYear = LocalDate(today.year, 1, 1).atStartOfDayIn(tz)

        return combine(
            dao.observeTotalCount(),
            dao.observeCountByShelf("CURRENTLY_READING"),
            dao.observeFinishedSince(startOfYear.toEpochMilliseconds()),
            dao.observeFinishedSince(startOfMonth.toEpochMilliseconds()),
            dao.observeFinishedBooks()
        ) { total, reading, finishedYear, finishedMonth, finishedBooks ->
            ReadingStats(
                totalBooks = total,
                currentlyReading = reading,
                finishedThisYear = finishedYear,
                finishedThisMonth = finishedMonth,
                streakDays = calculateStreak(finishedBooks.mapNotNull { it.finishedAt }, tz),
                monthlyFinished = buildMonthlyChart(finishedBooks.mapNotNull { it.finishedAt }, tz)
            )
        }
    }

    private fun calculateStreak(finishedTimestamps: List<Long>, tz: TimeZone): Int {
        if (finishedTimestamps.isEmpty()) return 0
        val dates = finishedTimestamps
            .map { Instant.fromEpochMilliseconds(it).toLocalDateTime(tz).date }
            .distinct()
            .sortedDescending()

        var streak = 1
        for (i in 0 until dates.size - 1) {
            val diff = dates[i].toEpochDays() - dates[i + 1].toEpochDays()
            if (diff.toInt() == 1) streak++ else break
        }
        return streak
    }

    private fun buildMonthlyChart(
        finishedTimestamps: List<Long>,
        tz: TimeZone
    ): kotlinx.collections.immutable.ImmutableList<MonthlyCount> {
        val now = Clock.System.now().toLocalDateTime(tz)
        val months = (0 until 6).reversed().map { offset ->
            var month = now.monthNumber - offset
            var year = now.year
            if (month <= 0) {
                month += 12
                year -= 1
            }
            year to month
        }

        val grouped = finishedTimestamps.groupBy { ts ->
            val dt = Instant.fromEpochMilliseconds(ts).toLocalDateTime(tz)
            dt.year to dt.monthNumber
        }

        return months.map { (year, month) ->
            val label = kotlinx.datetime.Month(month).name.take(3)
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            MonthlyCount(label = label, count = grouped[year to month]?.size ?: 0)
        }.toImmutableList()
    }
}
