package com.shelfie.core.data.di

import com.shelfie.core.data.repository.BookRepository
import com.shelfie.core.data.repository.BookRepositoryImpl
import com.shelfie.core.data.repository.ReadingListRepository
import com.shelfie.core.data.repository.ReadingListRepositoryImpl
import com.shelfie.core.data.repository.StatsRepository
import com.shelfie.core.data.repository.StatsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Unscoped on purpose: the repositories hold no state, Room is the single source of truth.
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    abstract fun bindReadingListRepository(impl: ReadingListRepositoryImpl): ReadingListRepository

    @Binds
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository
}
