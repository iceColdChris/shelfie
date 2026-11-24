package com.shelfie.core.network.model

import com.squareup.moshi.Json

/**
 * Top-level response from the Open Library search endpoint.
 */
data class NetworkSearchResponse(
    val numFound: Int?,
    val docs: List<NetworkBook>?
)

/**
 * DTO representing a single document from the Open Library search response.
 */
data class NetworkBook(
    val key: String,
    val title: String?,
    @Json(name = "author_name") val authorName: List<String>?,
    @Json(name = "cover_i") val coverId: Int?,
    val subject: List<String>? = null
)
