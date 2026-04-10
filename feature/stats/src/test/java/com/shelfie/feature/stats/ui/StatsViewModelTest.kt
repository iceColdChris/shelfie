package com.shelfie.feature.stats.ui

import com.shelfie.feature.stats.domain.model.MonthlyCount
import com.shelfie.feature.stats.domain.model.ReadingStats
import com.shelfie.feature.stats.domain.usecase.GetReadingStatsUseCase
import com.shelfie.feature.stats.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getReadingStatsUseCase: GetReadingStatsUseCase = mockk()

    @Test
    fun `initial state has isLoading true and no stats`() {
        every { getReadingStatsUseCase() } returns flowOf() // never emits
        val viewModel = StatsViewModel(getReadingStatsUseCase)
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.stats)
    }

    @Test
    fun `stats from use case populate uiState and clear isLoading`() = runTest {
        val stats = ReadingStats(
            totalBooks = 42,
            currentlyReading = 3,
            finishedThisYear = 15,
            finishedThisMonth = 2,
            streakDays = 7,
            monthlyFinished = persistentListOf(MonthlyCount("Jan", 1)),
        )
        every { getReadingStatsUseCase() } returns flowOf(stats)

        val viewModel = StatsViewModel(getReadingStatsUseCase)

        assertEquals(stats, viewModel.uiState.value.stats)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `uiState reflects the latest emission from the use case`() = runTest {
        val first = stats(totalBooks = 5)
        val second = stats(totalBooks = 10)
        every { getReadingStatsUseCase() } returns flowOf(first, second)

        val viewModel = StatsViewModel(getReadingStatsUseCase)

        assertEquals(10, viewModel.uiState.value.stats?.totalBooks)
    }

    // ---- Helpers ----

    private fun stats(totalBooks: Int = 0) = ReadingStats(
        totalBooks = totalBooks,
        currentlyReading = 0,
        finishedThisYear = 0,
        finishedThisMonth = 0,
        streakDays = 0,
        monthlyFinished = persistentListOf(),
    )
}
