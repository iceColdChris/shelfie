package com.shelfie.core.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.shelfie.core.data.database.AppDatabase
import com.shelfie.core.data.database.entity.CachedBookEntity
import com.shelfie.core.data.database.entity.FavoriteBookEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BookDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.bookDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---- observeCachedBooks ----

    @Test
    fun observeCachedBooks_returnsOnlyBooksForGivenQuery() = runTest {
        dao.insertCachedBooks(listOf(
            cachedBook(id = "dune-1", query = "dune"),
            cachedBook(id = "tolkien-1", query = "tolkien"),
        ))

        dao.observeCachedBooks("dune").test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("dune-1", books[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeCachedBooks_emitsEmptyListWhenNoMatch() = runTest {
        dao.observeCachedBooks("nonexistent").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeCachedBooks_emitsNewItemsReactively() = runTest {
        dao.observeCachedBooks("dune").test {
            assertTrue(awaitItem().isEmpty())

            dao.insertCachedBooks(listOf(cachedBook(id = "dune-1", query = "dune")))
            assertEquals(1, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- replaceCacheForQuery ----

    @Test
    fun replaceCacheForQuery_replacesExistingResultsForQuery() = runTest {
        dao.insertCachedBooks(listOf(
            cachedBook(id = "old-1", query = "dune"),
            cachedBook(id = "old-2", query = "dune"),
        ))

        dao.replaceCacheForQuery("dune", listOf(cachedBook(id = "new-1", query = "dune")))

        dao.observeCachedBooks("dune").test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("new-1", books[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun replaceCacheForQuery_doesNotAffectOtherQueries() = runTest {
        dao.insertCachedBooks(listOf(cachedBook(id = "tolkien-1", query = "tolkien")))

        dao.replaceCacheForQuery("dune", listOf(cachedBook(id = "dune-1", query = "dune")))

        dao.observeCachedBooks("tolkien").test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("tolkien-1", books[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun replaceCacheForQuery_withEmptyList_clearsExistingResults() = runTest {
        dao.insertCachedBooks(listOf(cachedBook(id = "dune-1", query = "dune")))

        dao.replaceCacheForQuery("dune", emptyList())

        dao.observeCachedBooks("dune").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- clearAllCache ----

    @Test
    fun clearAllCache_removesAllCachedBooksAcrossAllQueries() = runTest {
        dao.insertCachedBooks(listOf(
            cachedBook(id = "1", query = "dune"),
            cachedBook(id = "2", query = "tolkien"),
        ))

        dao.clearAllCache()

        dao.observeCachedBooks("dune").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        dao.observeCachedBooks("tolkien").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- favorites ----

    @Test
    fun insertFavorite_appearsInObserveFavoriteBooks() = runTest {
        dao.insertFavorite(favoriteBook(id = "fav-1"))

        dao.observeFavoriteBooks().test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("fav-1", books[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeFavorite_removesBookFromFavorites() = runTest {
        dao.insertFavorite(favoriteBook(id = "fav-1"))
        dao.removeFavorite("fav-1")

        dao.observeFavoriteBooks().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFavoriteBooks_emitsUpdatesReactively() = runTest {
        dao.observeFavoriteBooks().test {
            assertTrue(awaitItem().isEmpty())

            dao.insertFavorite(favoriteBook(id = "fav-1"))
            assertEquals(1, awaitItem().size)

            dao.removeFavorite("fav-1")
            assertTrue(awaitItem().isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertFavorite_replacesExistingEntryWithSameId() = runTest {
        dao.insertFavorite(favoriteBook(id = "fav-1", title = "Original Title"))
        dao.insertFavorite(favoriteBook(id = "fav-1", title = "Updated Title"))

        val book = dao.getFavoriteBookById("fav-1")
        assertEquals("Updated Title", book!!.title)
    }

    // ---- isFavorite ----

    @Test
    fun isFavorite_returnsTrueAfterInsert() = runTest {
        dao.insertFavorite(favoriteBook(id = "fav-1"))

        dao.isFavorite("fav-1").test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavorite_returnsFalseForNonFavorite() = runTest {
        dao.isFavorite("not-favorited").test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavorite_updatesReactivelyOnRemove() = runTest {
        dao.insertFavorite(favoriteBook(id = "fav-1"))

        dao.isFavorite("fav-1").test {
            assertTrue(awaitItem())

            dao.removeFavorite("fav-1")
            assertFalse(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- getCachedBookById ----

    @Test
    fun getCachedBookById_returnsCorrectBook() = runTest {
        dao.insertCachedBooks(listOf(cachedBook(id = "target", query = "q")))

        val book = dao.getCachedBookById("target")
        assertNotNull(book)
        assertEquals("target", book!!.id)
    }

    @Test
    fun getCachedBookById_returnsNullWhenNotFound() = runTest {
        assertNull(dao.getCachedBookById("nonexistent"))
    }

    // ---- getFavoriteBookById ----

    @Test
    fun getFavoriteBookById_returnsCorrectBook() = runTest {
        dao.insertFavorite(favoriteBook(id = "fav-1"))

        val book = dao.getFavoriteBookById("fav-1")
        assertNotNull(book)
        assertEquals("fav-1", book!!.id)
    }

    @Test
    fun getFavoriteBookById_returnsNullWhenNotFound() = runTest {
        assertNull(dao.getFavoriteBookById("nonexistent"))
    }

    // ---- Helpers ----

    private fun cachedBook(
        id: String,
        title: String = "A Title",
        query: String,
    ) = CachedBookEntity(
        id = id,
        title = title,
        authors = listOf("An Author"),
        thumbnail = null,
        query = query,
        subjects = emptyList(),
    )

    private fun favoriteBook(
        id: String,
        title: String = "Fav Title",
    ) = FavoriteBookEntity(
        id = id,
        title = title,
        authors = listOf("An Author"),
        thumbnail = null,
    )
}
