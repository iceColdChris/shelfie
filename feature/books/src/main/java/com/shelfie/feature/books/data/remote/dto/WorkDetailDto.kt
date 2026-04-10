package com.shelfie.feature.books.data.remote.dto

/**
 * DTO for the Open Library Works detail endpoint (/works/{id}.json).
 * The [description] field is polymorphic: it can be a plain JSON string
 * or an object with a "value" key. Mapping handles both cases.
 */
data class WorkDetailDto(
    val key: String,
    val description: Any? = null,
    val subjects: List<String>? = null,
    val covers: List<Int>? = null
)
