package com.shelfie.feature.stats.domain.usecase

import com.shelfie.feature.stats.domain.model.ReadingStats
import com.shelfie.feature.stats.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes aggregated reading statistics from the local database. */
class GetReadingStatsUseCase @Inject constructor(
    private val repository: StatsRepository
) {
    operator fun invoke(): Flow<ReadingStats> = repository.observeStats()
}
