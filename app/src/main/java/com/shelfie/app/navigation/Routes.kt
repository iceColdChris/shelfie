package com.shelfie.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Routes are @Serializable so the back stacks survive process death.

@Serializable
data object SearchRoute : NavKey

@Serializable
data object LibraryRoute : NavKey

@Serializable
data object StatsRoute : NavKey

@Serializable
data class BookDetailRoute(val bookId: String) : NavKey
