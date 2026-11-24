package com.shelfie.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.shelfie.app.navigation.LibraryRoute
import com.shelfie.app.navigation.SearchRoute
import com.shelfie.app.navigation.StatsRoute

/**
 * Enumerates the three top-level destinations shown in the bottom navigation bar.
 */
enum class TopLevelDestination(
    val route: NavKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    SEARCH(
        route = SearchRoute,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        label = "Search"
    ),
    LIBRARY(
        route = LibraryRoute,
        selectedIcon = Icons.Filled.CollectionsBookmark,
        unselectedIcon = Icons.Outlined.CollectionsBookmark,
        label = "Library"
    ),
    STATS(
        route = StatsRoute,
        selectedIcon = Icons.Filled.Insights,
        unselectedIcon = Icons.Outlined.Insights,
        label = "Stats"
    )
}
