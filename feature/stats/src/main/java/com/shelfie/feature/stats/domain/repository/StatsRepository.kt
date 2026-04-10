package com.shelfie.feature.stats.domain.repository

import com.shelfie.feature.stats.domain.model.ReadingStats
import kotlinx.coroutines.flow.Flow

/** Repository contract for reading statistics. */
interface StatsRepository {
    /** Observes aggregated reading statistics. */
    fun observeStats(): Flow<ReadingStats>
}
