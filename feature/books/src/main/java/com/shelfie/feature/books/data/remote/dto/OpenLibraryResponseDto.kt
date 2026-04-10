package com.shelfie.feature.books.data.remote.dto

import com.squareup.moshi.Json

/**
 * Top-level response from the Open Library search endpoint.
 */
data class OpenLibraryResponseDto(
    val numFound: Int?,
    val docs: List<OpenLibraryBookDto>?
)

/**
 * DTO representing a single document from the Open Library search response.
 */
data class OpenLibraryBookDto(
    val key: String,
    val title: String?,
    @Json(name = "author_name") val authorName: List<String>?,
    @Json(name = "cover_i") val coverId: Int?,
    val subject: List<String>? = null
)
