package com.shelfie.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import com.shelfie.core.database.converter.StringListConverter
import com.shelfie.core.database.dao.BookDao
import com.shelfie.core.database.dao.ReadingListDao
import com.shelfie.core.database.model.CachedBookEntity
import com.shelfie.core.database.model.FavoriteBookEntity
import com.shelfie.core.database.model.ReadingListEntity

/**
 * Main Room database for the application.
 */
@Database(
    entities = [
        FavoriteBookEntity::class,
        CachedBookEntity::class,
        ReadingListEntity::class
    ],
    version = 4,
    exportSchema = false
)
@ColumnTypeConverters(StringListConverter::class)
internal abstract class ShelfieDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readingListDao(): ReadingListDao
}
