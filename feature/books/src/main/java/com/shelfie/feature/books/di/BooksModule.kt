package com.shelfie.feature.books.di

import com.shelfie.feature.books.data.remote.OpenLibraryApi
import com.shelfie.feature.books.data.repository.BookRepositoryImpl
import com.shelfie.feature.books.domain.repository.BookRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class BooksModule {

    @Binds
    @ViewModelScoped
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    companion object {
        @Provides
        @ViewModelScoped
        fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi {
            return retrofit.create(OpenLibraryApi::class.java)
        }
    }
}
