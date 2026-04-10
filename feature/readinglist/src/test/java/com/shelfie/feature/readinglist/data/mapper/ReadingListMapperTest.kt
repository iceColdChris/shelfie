package com.shelfie.feature.readinglist.data.mapper

import com.shelfie.core.data.database.entity.ReadingListEntry
import com.shelfie.feature.readinglist.data.mapper.ReadingListMapper.toDomain
import com.shelfie.feature.readinglist.data.mapper.ReadingListMapper.toEntity
import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class ReadingListMapperTest {

    // ---- ReadingListEntry → ReadingListBook ----

    @Test
    fun `toDomain maps all basic fields correctly`() {
        val entry = entry(bookId = "b1", title = "Dune", shelf = "WANT_TO_READ", progress = 42, sortOrder = 3)
        val book = entry.toDomain()
        assertEquals("b1", book.bookId)
        assertEquals("Dune", book.title)
        assertEquals(listOf("Frank Herbert"), book.authors.toList())
        assertEquals(Shelf.WANT_TO_READ, book.shelf)
        assertEquals(42, book.progress)
        assertEquals(3, book.sortOrder)
    }

    @Test
    fun `toDomain converts addedAt epoch millis to Instant`() {
        val epochMs = 1_700_000_000_000L
        val book = entry(addedAt = epochMs).toDomain()
        assertEquals(Instant.fromEpochMilliseconds(epochMs), book.addedAt)
    }

    @Test
    fun `toDomain converts non-null finishedAt to Instant`() {
        val epochMs = 1_710_000_000_000L
        val book = entry(finishedAt = epochMs).toDomain()
        assertNotNull(book.finishedAt)
        assertEquals(Instant.fromEpochMilliseconds(epochMs), book.finishedAt)
    }

    @Test
    fun `toDomain maps null finishedAt to null`() {
        val book = entry(finishedAt = null).toDomain()
        assertNull(book.finishedAt)
    }

    @Test
    fun `toDomain resolves CURRENTLY_READING shelf`() {
        assertEquals(Shelf.CURRENTLY_READING, entry(shelf = "CURRENTLY_READING").toDomain().shelf)
    }

    @Test
    fun `toDomain resolves FINISHED shelf`() {
        assertEquals(Shelf.FINISHED, entry(shelf = "FINISHED").toDomain().shelf)
    }

    @Test
    fun `toDomain resolves WANT_TO_READ shelf`() {
        assertEquals(Shelf.WANT_TO_READ, entry(shelf = "WANT_TO_READ").toDomain().shelf)
    }

    // ---- ReadingListBook → ReadingListEntry ----

    @Test
    fun `toEntity maps all basic fields correctly`() {
        val book = book(bookId = "b1", title = "Dune", shelf = Shelf.CURRENTLY_READING, progress = 55, sortOrder = 1)
        val entity = book.toEntity()
        assertEquals("b1", entity.bookId)
        assertEquals("Dune", entity.title)
        assertEquals("CURRENTLY_READING", entity.shelf)
        assertEquals(55, entity.progress)
        assertEquals(1, entity.sortOrder)
    }

    @Test
    fun `toEntity converts addedAt Instant to epoch millis`() {
        val instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
        val entity = book(addedAt = instant).toEntity()
        assertEquals(1_700_000_000_000L, entity.addedAt)
    }

    @Test
    fun `toEntity converts non-null finishedAt Instant to epoch millis`() {
        val instant = Instant.fromEpochMilliseconds(1_710_000_000_000L)
        val entity = book(finishedAt = instant).toEntity()
        assertEquals(1_710_000_000_000L, entity.finishedAt)
    }

    @Test
    fun `toEntity maps null finishedAt to null`() {
        val entity = book(finishedAt = null).toEntity()
        assertNull(entity.finishedAt)
    }

    @Test
    fun `toDomain and toEntity are inverse operations`() {
        val original = entry(bookId = "b1", title = "Dune", shelf = "FINISHED", progress = 100, sortOrder = 0, finishedAt = 1_710_000_000_000L)
        val roundtripped = original.toDomain().toEntity()
        assertEquals(original.bookId, roundtripped.bookId)
        assertEquals(original.title, roundtripped.title)
        assertEquals(original.shelf, roundtripped.shelf)
        assertEquals(original.progress, roundtripped.progress)
        assertEquals(original.addedAt, roundtripped.addedAt)
        assertEquals(original.finishedAt, roundtripped.finishedAt)
    }

    // ---- Shelf.fromValue ----

    @Test
    fun `Shelf fromValue resolves all valid values`() {
        assertEquals(Shelf.WANT_TO_READ, Shelf.fromValue("WANT_TO_READ"))
        assertEquals(Shelf.CURRENTLY_READING, Shelf.fromValue("CURRENTLY_READING"))
        assertEquals(Shelf.FINISHED, Shelf.fromValue("FINISHED"))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Shelf fromValue throws for unknown value`() {
        Shelf.fromValue("INVALID")
    }

    // ---- Helpers ----

    private fun entry(
        bookId: String = "b1",
        title: String = "Dune",
        shelf: String = "WANT_TO_READ",
        progress: Int = 0,
        sortOrder: Int = 0,
        addedAt: Long = 1_700_000_000_000L,
        finishedAt: Long? = null,
    ) = ReadingListEntry(
        bookId = bookId,
        title = title,
        authors = listOf("Frank Herbert"),
        thumbnail = null,
        shelf = shelf,
        progress = progress,
        sortOrder = sortOrder,
        addedAt = addedAt,
        finishedAt = finishedAt,
    )

    private fun book(
        bookId: String = "b1",
        title: String = "Dune",
        shelf: Shelf = Shelf.WANT_TO_READ,
        progress: Int = 0,
        sortOrder: Int = 0,
        addedAt: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        finishedAt: Instant? = null,
    ) = ReadingListBook(
        bookId = bookId,
        title = title,
        authors = persistentListOf("Frank Herbert"),
        thumbnail = null,
        shelf = shelf,
        progress = progress,
        sortOrder = sortOrder,
        addedAt = addedAt,
        finishedAt = finishedAt,
    )
}
