package com.shelfie.core.data.di

import android.content.Context
import androidx.room.Room
import com.shelfie.core.data.database.AppDatabase
import com.shelfie.core.data.database.dao.BookDao
import com.shelfie.core.data.database.dao.ReadingListDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "shelfie_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
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
