package com.shelfie.feature.stats.di

import com.shelfie.feature.stats.data.repository.StatsRepositoryImpl
import com.shelfie.feature.stats.domain.repository.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class StatsModule {

    @Binds
    @ViewModelScoped
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository
}
