package com.shelfie.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shelfie.core.data.database.converter.Converters
import com.shelfie.core.data.database.dao.BookDao
import com.shelfie.core.data.database.dao.ReadingListDao
import com.shelfie.core.data.database.entity.CachedBookEntity
import com.shelfie.core.data.database.entity.FavoriteBookEntity
import com.shelfie.core.data.database.entity.ReadingListEntry

/**
 * Main Room database for the application.
 */
@Database(
    entities = [
        FavoriteBookEntity::class,
        CachedBookEntity::class,
        ReadingListEntry::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readingListDao(): ReadingListDao
}
