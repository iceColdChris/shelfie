package com.shelfie.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Nav keys are @Serializable so the back stacks survive process death.

@Serializable
data object BooksNavKey : NavKey

@Serializable
data object ReadingListNavKey : NavKey

@Serializable
data object StatsNavKey : NavKey

@Serializable
data class BookDetailNavKey(val bookId: String) : NavKey
