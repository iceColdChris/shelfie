package com.shelfie.feature.stats.ui

import androidx.compose.runtime.Immutable
import com.shelfie.feature.stats.domain.model.ReadingStats

@Immutable
data class StatsUiState(
    val stats: ReadingStats? = null,
    val isLoading: Boolean = true
)
