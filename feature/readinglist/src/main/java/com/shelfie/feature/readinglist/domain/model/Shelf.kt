package com.shelfie.feature.readinglist.domain.model

/**
 * Predefined shelves a book can be placed on.
 * The [value] matches the string stored in Room.
 */
enum class Shelf(val value: String, val displayName: String) {
    WANT_TO_READ("WANT_TO_READ", "Want to Read"),
    CURRENTLY_READING("CURRENTLY_READING", "Currently Reading"),
    FINISHED("FINISHED", "Finished");

    companion object {
        fun fromValue(value: String): Shelf =
            entries.first { it.value == value }
    }
}
