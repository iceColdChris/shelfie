package com.shelfie.feature.stats.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ReadingStats(
    val totalBooks: Int,
    val currentlyReading: Int,
    val finishedThisYear: Int,
    val finishedThisMonth: Int,
    val streakDays: Int,
    val monthlyFinished: ImmutableList<MonthlyCount>
)

@Immutable
data class MonthlyCount(
    val label: String,
    val count: Int
)
