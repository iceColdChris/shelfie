package com.shelfie.feature.stats

import androidx.compose.runtime.Immutable
import com.shelfie.core.model.ReadingStats

@Immutable
data class StatsUiState(
    val stats: ReadingStats? = null,
    val isLoading: Boolean = true
)
