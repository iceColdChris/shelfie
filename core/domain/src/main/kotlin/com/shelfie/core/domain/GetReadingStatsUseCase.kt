package com.shelfie.core.domain

import com.shelfie.core.model.ReadingStats
import com.shelfie.core.data.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes aggregated reading statistics from the local database. */
class GetReadingStatsUseCase @Inject constructor(
    private val repository: StatsRepository
) {
    operator fun invoke(): Flow<ReadingStats> = repository.observeStats()
}
