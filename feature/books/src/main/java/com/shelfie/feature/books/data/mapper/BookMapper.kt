package com.shelfie.feature.books.data.mapper

import com.shelfie.core.data.database.entity.CachedBookEntity
import com.shelfie.core.data.database.entity.FavoriteBookEntity
import com.shelfie.feature.books.data.remote.dto.OpenLibraryBookDto
import com.shelfie.feature.books.data.remote.dto.WorkDetailDto
import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.model.BookDetail
import kotlinx.collections.immutable.toImmutableList

/**
 * Maps between DTOs, Room entities, and domain models.
 */
internal object BookMapper {

    private val MARKDOWN_LINK = Regex("\\[([^]]+)]\\([^)]+\\)")
    private val FOOTNOTE_REF = Regex("\\[\\d+]:.*")

    private const val MAX_SUBJECTS = 10

    fun OpenLibraryBookDto.toCachedEntity(query: String): CachedBookEntity {
        return CachedBookEntity(
            id = key,
            title = title ?: "",
            authors = authorName ?: emptyList(),
            thumbnail = coverId?.let { buildCoverUrl(it) },
            query = query,
            subjects = subject?.take(MAX_SUBJECTS) ?: emptyList()
        )
    }

    fun CachedBookEntity.toDomain(isFavorite: Boolean = false): Book {
        return Book(
            id = id,
            title = title,
            authors = authors.toImmutableList(),
            thumbnail = thumbnail,
            isFavorite = isFavorite
        )
    }

    fun FavoriteBookEntity.toDomain(): Book {
        return Book(
            id = id,
            title = title,
            authors = authors.toImmutableList(),
            thumbnail = thumbnail,
            isFavorite = true
        )
    }

    fun Book.toFavoriteEntity(): FavoriteBookEntity {
        return FavoriteBookEntity(
            id = id,
            title = title,
            authors = authors,
            thumbnail = thumbnail
        )
    }

    /**
     * Builds a [BookDetail] by merging the Works API response with locally cached data.
     * Subjects are sourced from the search API cache (which returns English-weighted results)
     * and fall back to the Works API subjects if the cache is empty.
     */
    fun WorkDetailDto.toBookDetail(
        book: Book,
        cachedSubjects: List<String>
    ): BookDetail {
        val resolvedSubjects = cachedSubjects.ifEmpty {
            subjects?.take(MAX_SUBJECTS) ?: emptyList()
        }
        return BookDetail(
            id = book.id,
            title = book.title,
            authors = book.authors,
            thumbnail = book.thumbnail,
            description = extractDescription(description),
            subjects = resolvedSubjects.toImmutableList(),
            isFavorite = book.isFavorite
        )
    }

    /**
     * The Open Library description field is polymorphic: it can be a plain
     * JSON string or an object with a "value" key.
     */
    @Suppress("UNCHECKED_CAST")
    private fun extractDescription(raw: Any?): String? {
        val text = when (raw) {
            is String -> raw
            is Map<*, *> -> (raw as? Map<String, Any?>)?.get("value") as? String
            else -> null
        }
        return text?.cleanDescription()
    }

    /**
     * Cleans up Open Library descriptions which may contain carriage returns
     * and markdown-style link syntax.
     */
    private fun String.cleanDescription(): String {
        return this
            .replace("\r\n", "\n")
            .replace(MARKDOWN_LINK, "$1")
            .replace(FOOTNOTE_REF, "")
            .trim()
    }

    /** Builds a cover image URL from an Open Library cover ID. */
    private fun buildCoverUrl(coverId: Int): String {
        return "https://covers.openlibrary.org/b/id/$coverId-M.jpg"
    }
}
