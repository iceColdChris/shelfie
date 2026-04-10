package com.shelfie.feature.books.data.mapper

import com.shelfie.core.data.database.entity.CachedBookEntity
import com.shelfie.core.data.database.entity.FavoriteBookEntity
import com.shelfie.feature.books.data.mapper.BookMapper.toBookDetail
import com.shelfie.feature.books.data.mapper.BookMapper.toCachedEntity
import com.shelfie.feature.books.data.mapper.BookMapper.toDomain
import com.shelfie.feature.books.data.mapper.BookMapper.toFavoriteEntity
import com.shelfie.feature.books.data.remote.dto.OpenLibraryBookDto
import com.shelfie.feature.books.data.remote.dto.WorkDetailDto
import com.shelfie.feature.books.domain.model.Book
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookMapperTest {

    // ---- OpenLibraryBookDto → CachedBookEntity ----

    @Test
    fun `toCachedEntity maps id and query`() {
        val entity = dto(key = "/works/OL1W").toCachedEntity("my-query")
        assertEquals("/works/OL1W", entity.id)
        assertEquals("my-query", entity.query)
    }

    @Test
    fun `toCachedEntity uses empty string for null title`() {
        val entity = dto(title = null).toCachedEntity("q")
        assertEquals("", entity.title)
    }

    @Test
    fun `toCachedEntity builds thumbnail URL from coverId`() {
        val entity = dto(coverId = 99999).toCachedEntity("q")
        assertEquals("https://covers.openlibrary.org/b/id/99999-M.jpg", entity.thumbnail)
    }

    @Test
    fun `toCachedEntity sets null thumbnail when coverId is absent`() {
        val entity = dto(coverId = null).toCachedEntity("q")
        assertNull(entity.thumbnail)
    }

    @Test
    fun `toCachedEntity limits subjects to 10`() {
        val entity = dto(subjects = (1..20).map { "Subject $it" }).toCachedEntity("q")
        assertEquals(10, entity.subjects.size)
    }

    @Test
    fun `toCachedEntity maps empty authors when authorName is null`() {
        val entity = dto(authorName = null).toCachedEntity("q")
        assertTrue(entity.authors.isEmpty())
    }

    @Test
    fun `toCachedEntity maps author list correctly`() {
        val entity = dto(authorName = listOf("Frank Herbert", "Brian Herbert")).toCachedEntity("q")
        assertEquals(listOf("Frank Herbert", "Brian Herbert"), entity.authors)
    }

    // ---- CachedBookEntity → Book ----

    @Test
    fun `CachedBookEntity toDomain maps all fields`() {
        val cached = CachedBookEntity(
            id = "id", title = "Dune", authors = listOf("Frank Herbert"),
            thumbnail = "url", query = "q"
        )
        val book = cached.toDomain()
        assertEquals("id", book.id)
        assertEquals("Dune", book.title)
        assertEquals(listOf("Frank Herbert"), book.authors.toList())
        assertEquals("url", book.thumbnail)
        assertFalse(book.isFavorite)
    }

    @Test
    fun `CachedBookEntity toDomain defaults isFavorite to false`() {
        assertFalse(cachedEntity().toDomain().isFavorite)
    }

    @Test
    fun `CachedBookEntity toDomain propagates isFavorite true`() {
        assertTrue(cachedEntity().toDomain(isFavorite = true).isFavorite)
    }

    // ---- FavoriteBookEntity → Book ----

    @Test
    fun `FavoriteBookEntity toDomain always sets isFavorite true`() {
        val fav = FavoriteBookEntity(id = "fav", title = "Fav", authors = listOf("Author"), thumbnail = null)
        val book = fav.toDomain()
        assertEquals("fav", book.id)
        assertTrue(book.isFavorite)
    }

    // ---- Book → FavoriteBookEntity ----

    @Test
    fun `toFavoriteEntity maps all fields from Book`() {
        val book = Book(id = "bid", title = "Title", authors = persistentListOf("A", "B"), thumbnail = "thumb")
        val entity = book.toFavoriteEntity()
        assertEquals("bid", entity.id)
        assertEquals("Title", entity.title)
        assertEquals(listOf("A", "B"), entity.authors)
        assertEquals("thumb", entity.thumbnail)
    }

    // ---- WorkDetailDto → BookDetail ----

    @Test
    fun `toBookDetail uses plain string description`() {
        val detail = WorkDetailDto(key = "/works/OL1W", description = "A plain description.").toBookDetail(book(), emptyList())
        assertEquals("A plain description.", detail.description)
    }

    @Test
    fun `toBookDetail reads description from map value key`() {
        val desc = mapOf("type" to "/type/text", "value" to "From a map.")
        val detail = WorkDetailDto(key = "/works/OL1W", description = desc).toBookDetail(book(), emptyList())
        assertEquals("From a map.", detail.description)
    }

    @Test
    fun `toBookDetail returns null description for unsupported type`() {
        val detail = WorkDetailDto(key = "/works/OL1W", description = 42).toBookDetail(book(), emptyList())
        assertNull(detail.description)
    }

    @Test
    fun `toBookDetail returns null description when field is null`() {
        val detail = WorkDetailDto(key = "/works/OL1W", description = null).toBookDetail(book(), emptyList())
        assertNull(detail.description)
    }

    @Test
    fun `toBookDetail prefers cached subjects over works subjects`() {
        val dto = WorkDetailDto(key = "/works/OL1W", subjects = listOf("Works Subject"))
        val detail = dto.toBookDetail(book(), listOf("Cached Subject"))
        assertEquals(listOf("Cached Subject"), detail.subjects.toList())
    }

    @Test
    fun `toBookDetail falls back to works subjects when cache is empty`() {
        val dto = WorkDetailDto(key = "/works/OL1W", subjects = listOf("Works Subject"))
        val detail = dto.toBookDetail(book(), emptyList())
        assertEquals(listOf("Works Subject"), detail.subjects.toList())
    }

    @Test
    fun `toBookDetail limits works subjects to 10`() {
        val dto = WorkDetailDto(key = "/works/OL1W", subjects = (1..15).map { "S$it" })
        val detail = dto.toBookDetail(book(), emptyList())
        assertEquals(10, detail.subjects.size)
    }

    @Test
    fun `toBookDetail propagates book fields`() {
        val b = book()
        val detail = WorkDetailDto(key = b.id).toBookDetail(b, emptyList())
        assertEquals(b.id, detail.id)
        assertEquals(b.title, detail.title)
        assertEquals(b.authors, detail.authors)
        assertEquals(b.thumbnail, detail.thumbnail)
        assertEquals(b.isFavorite, detail.isFavorite)
    }

    // ---- Description cleaning ----

    @Test
    fun `description cleaning replaces carriage returns`() {
        val detail = workDto("Line one\r\nLine two").toBookDetail(book(), emptyList())
        assertEquals("Line one\nLine two", detail.description)
    }

    @Test
    fun `description cleaning strips markdown links`() {
        val detail = workDto("See [OpenLibrary](https://openlibrary.org) here.").toBookDetail(book(), emptyList())
        assertEquals("See OpenLibrary here.", detail.description)
    }

    @Test
    fun `description cleaning strips footnote references`() {
        val detail = workDto("Some text.\n[1]: https://example.com").toBookDetail(book(), emptyList())
        assertEquals("Some text.", detail.description)
    }

    @Test
    fun `description cleaning trims surrounding whitespace`() {
        val detail = workDto("  trimmed  ").toBookDetail(book(), emptyList())
        assertEquals("trimmed", detail.description)
    }

    // ---- Helpers ----

    private fun dto(
        key: String = "/works/OL1W",
        title: String? = "A Title",
        authorName: List<String>? = listOf("An Author"),
        coverId: Int? = null,
        subjects: List<String>? = null,
    ) = OpenLibraryBookDto(key = key, title = title, authorName = authorName, coverId = coverId, subject = subjects)

    private fun cachedEntity(
        id: String = "id",
        title: String = "Title",
    ) = CachedBookEntity(id = id, title = title, authors = listOf("Author"), thumbnail = null, query = "q")

    private fun book() = Book(
        id = "/works/OL1W",
        title = "Title",
        authors = persistentListOf("Author"),
        thumbnail = "https://example.com/cover.jpg",
        isFavorite = false,
    )

    private fun workDto(description: String) = WorkDetailDto(key = "/works/OL1W", description = description)
}
