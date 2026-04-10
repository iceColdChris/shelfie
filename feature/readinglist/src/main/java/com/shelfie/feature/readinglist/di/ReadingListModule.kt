package com.shelfie.feature.readinglist.di

import com.shelfie.feature.readinglist.data.repository.ReadingListRepositoryImpl
import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class ReadingListModule {

    @Binds
    @ViewModelScoped
    abstract fun bindReadingListRepository(impl: ReadingListRepositoryImpl): ReadingListRepository
}
