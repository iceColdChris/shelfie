package com.shelfie.core.database.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.shelfie.core.database.AppDatabase
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // AndroidSQLiteDriver keeps the platform SQLite that wrote the existing database file;
        // BundledSQLiteDriver would add a native library for features this app does not use.
        return Room.databaseBuilder<AppDatabase>(context, "shelfie_db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    fun provideReadingListDao(database: AppDatabase): ReadingListDao {
        return database.readingListDao()
    }
}
