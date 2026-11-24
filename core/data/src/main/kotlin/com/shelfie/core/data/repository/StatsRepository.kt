package com.shelfie.core.data.repository

import com.shelfie.core.model.ReadingStats
import kotlinx.coroutines.flow.Flow

/** Repository contract for reading statistics. */
interface StatsRepository {
    /** Observes aggregated reading statistics. */
    fun observeStats(): Flow<ReadingStats>
}
