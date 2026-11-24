package com.shelfie.core.database.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.shelfie.core.database.ShelfieDatabase
import com.shelfie.core.database.dao.BookDao
import com.shelfie.core.database.dao.ReadingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideShelfieDatabase(@ApplicationContext context: Context): ShelfieDatabase {
        // AndroidSQLiteDriver keeps the platform SQLite that wrote the existing database file;
        // BundledSQLiteDriver would add a native library for features this app does not use.
        return Room.databaseBuilder<ShelfieDatabase>(context, "shelfie_db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    @Provides
    fun provideBookDao(database: ShelfieDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    fun provideReadingListDao(database: ShelfieDatabase): ReadingListDao {
        return database.readingListDao()
    }
}
