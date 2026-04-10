package com.shelfie.feature.books.data.repository

import app.cash.turbine.test
import com.shelfie.core.data.database.dao.BookDao
import com.shelfie.core.data.database.entity.CachedBookEntity
import com.shelfie.core.data.database.entity.FavoriteBookEntity
import com.shelfie.feature.books.data.remote.OpenLibraryApi
import com.shelfie.feature.books.data.remote.dto.OpenLibraryBookDto
import com.shelfie.feature.books.data.remote.dto.OpenLibraryResponseDto
import com.shelfie.feature.books.data.remote.dto.WorkDetailDto
import com.shelfie.feature.books.domain.model.Book
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryImplTest {

    private val api: OpenLibraryApi = mockk()
    private val bookDao: BookDao = mockk()
    private val repository = BookRepositoryImpl(api, bookDao)

    // ---- refreshSearch ----

    @Test
    fun `refreshSearch calls API and writes mapped entities to cache`() = runTest {
        val dto = bookDto(key = "/works/OL1W", title = "Dune")
        coEvery { api.searchBooks(query = "dune", limit = any(), fields = any()) } returns
            OpenLibraryResponseDto(numFound = 1, docs = listOf(dto))
        coEvery { bookDao.replaceCacheForQuery(any(), any()) } returns Unit

        repository.refreshSearch("dune")

        coVerify {
            bookDao.replaceCacheForQuery(
                "dune",
                match { entities -> entities.size == 1 && entities[0].id == "/works/OL1W" },
            )
        }
    }

    @Test
    fun `refreshSearch writes empty list when API returns null docs`() = runTest {
        coEvery { api.searchBooks(query = "empty", limit = any(), fields = any()) } returns
            OpenLibraryResponseDto(numFound = 0, docs = null)
        coEvery { bookDao.replaceCacheForQuery(any(), any()) } returns Unit

        repository.refreshSearch("empty")

        coVerify { bookDao.replaceCacheForQuery("empty", emptyList()) }
    }

    // ---- observeSearchResults ----

    @Test
    fun `observeSearchResults marks books present in favorites as isFavorite`() = runTest {
        val cached = listOf(
            cachedEntity(id = "book-1"),
            cachedEntity(id = "book-2"),
        )
        val favorites = listOf(favEntity(id = "book-1"))
        every { bookDao.observeCachedBooks("q") } returns flowOf(cached)
        every { bookDao.observeFavoriteBooks() } returns flowOf(favorites)

        repository.observeSearchResults("q").test {
            val books = awaitItem()
            assertEquals(2, books.size)
            assertTrue(books.first { it.id == "book-1" }.isFavorite)
            assertFalse(books.first { it.id == "book-2" }.isFavorite)
            awaitComplete()
        }
    }

    @Test
    fun `observeSearchResults emits empty list when cache is empty`() = runTest {
        every { bookDao.observeCachedBooks("q") } returns flowOf(emptyList())
        every { bookDao.observeFavoriteBooks() } returns flowOf(emptyList())

        repository.observeSearchResults("q").test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    // ---- observeFavorites ----

    @Test
    fun `observeFavorites maps entities and sets isFavorite true`() = runTest {
        val entities = listOf(favEntity(id = "fav-1", title = "Fav Book"))
        every { bookDao.observeFavoriteBooks() } returns flowOf(entities)

        repository.observeFavorites().test {
            val books = awaitItem()
            assertEquals(1, books.size)
            assertEquals("fav-1", books[0].id)
            assertEquals("Fav Book", books[0].title)
            assertTrue(books[0].isFavorite)
            awaitComplete()
        }
    }

    // ---- addFavorite / removeFavorite ----

    @Test
    fun `addFavorite inserts mapped entity into dao`() = runTest {
        val book = Book(id = "id", title = "Title", authors = persistentListOf("A"), thumbnail = "url")
        coEvery { bookDao.insertFavorite(any()) } returns Unit

        repository.addFavorite(book)

        coVerify { bookDao.insertFavorite(match { it.id == "id" && it.title == "Title" }) }
    }

    @Test
    fun `removeFavorite delegates to dao`() = runTest {
        coEvery { bookDao.removeFavorite("id") } returns Unit

        repository.removeFavorite("id")

        coVerify(exactly = 1) { bookDao.removeFavorite("id") }
    }

    // ---- getCachedBook ----

    @Test
    fun `getCachedBook returns null when nothing in db`() = runTest {
        coEvery { bookDao.getCachedBookById("id") } returns null
        coEvery { bookDao.getFavoriteBookById("id") } returns null

        assertNull(repository.getCachedBook("id"))
    }

    @Test
    fun `getCachedBook returns cached book marked as favorite when in both tables`() = runTest {
        coEvery { bookDao.getCachedBookById("id") } returns cachedEntity(id = "id")
        coEvery { bookDao.getFavoriteBookById("id") } returns favEntity(id = "id")

        val result = repository.getCachedBook("id")

        assertNotNull(result)
        assertTrue(result!!.isFavorite)
    }

    @Test
    fun `getCachedBook returns cached book not marked as favorite when not in favorites`() = runTest {
        coEvery { bookDao.getCachedBookById("id") } returns cachedEntity(id = "id")
        coEvery { bookDao.getFavoriteBookById("id") } returns null

        val result = repository.getCachedBook("id")

        assertNotNull(result)
        assertFalse(result!!.isFavorite)
    }

    @Test
    fun `getCachedBook falls back to favorite entity when not in cache`() = runTest {
        coEvery { bookDao.getCachedBookById("id") } returns null
        coEvery { bookDao.getFavoriteBookById("id") } returns favEntity(id = "id", title = "Fav Only")

        val result = repository.getCachedBook("id")

        assertNotNull(result)
        assertEquals("Fav Only", result!!.title)
        assertTrue(result.isFavorite)
    }

    // ---- getBookDetail ----

    @Test
    fun `getBookDetail extracts workId and calls API`() = runTest {
        val bookId = "/works/OL123W"
        coEvery { bookDao.getCachedBookById(bookId) } returns cachedEntity(id = bookId, title = "Dune")
        coEvery { bookDao.getFavoriteBookById(bookId) } returns null
        coEvery { api.getWorkDetail("OL123W") } returns WorkDetailDto(
            key = bookId, description = "A great book.", subjects = listOf("Science Fiction")
        )

        val detail = repository.getBookDetail(bookId)

        assertEquals("Dune", detail.title)
        assertEquals("A great book.", detail.description)
    }

    @Test
    fun `getBookDetail prefers cached subjects over works subjects`() = runTest {
        val bookId = "/works/OL123W"
        coEvery { bookDao.getCachedBookById(bookId) } returns
            cachedEntity(id = bookId, subjects = listOf("Cached SF"))
        coEvery { bookDao.getFavoriteBookById(bookId) } returns null
        coEvery { api.getWorkDetail("OL123W") } returns WorkDetailDto(
            key = bookId, subjects = listOf("Works SF")
        )

        val detail = repository.getBookDetail(bookId)

        assertEquals(listOf("Cached SF"), detail.subjects.toList())
    }

    @Test
    fun `getBookDetail uses EMPTY book when not in cache or favorites`() = runTest {
        val bookId = "/works/OL999W"
        coEvery { bookDao.getCachedBookById(bookId) } returns null
        coEvery { bookDao.getFavoriteBookById(bookId) } returns null
        coEvery { api.getWorkDetail("OL999W") } returns WorkDetailDto(key = bookId)

        val detail = repository.getBookDetail(bookId)

        assertEquals(bookId, detail.id)
    }

    // ---- Helpers ----

    private fun bookDto(key: String = "/works/OL1W", title: String = "Title") =
        OpenLibraryBookDto(key = key, title = title, authorName = listOf("Author"), coverId = null)

    private fun cachedEntity(
        id: String = "id",
        title: String = "Title",
        subjects: List<String> = emptyList(),
    ) = CachedBookEntity(id = id, title = title, authors = listOf("Author"), thumbnail = null, query = "q", subjects = subjects)

    private fun favEntity(id: String = "id", title: String = "Fav") =
        FavoriteBookEntity(id = id, title = title, authors = listOf("Author"), thumbnail = null)
}
