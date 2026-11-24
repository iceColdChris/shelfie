package com.shelfie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.shelfie.app.navigation.BookDetailRoute
import com.shelfie.app.navigation.LibraryRoute
import com.shelfie.app.navigation.Navigator
import com.shelfie.app.navigation.SearchRoute
import com.shelfie.app.navigation.StatsRoute
import com.shelfie.app.navigation.rememberNavigationState
import com.shelfie.app.navigation.toEntries
import com.shelfie.core.ui.theme.ShelfieTheme
import com.shelfie.feature.books.ui.BookDetailScreen
import com.shelfie.feature.books.ui.BookDetailViewModel
import com.shelfie.feature.books.ui.BooksScreen
import com.shelfie.feature.readinglist.ui.ReadingListScreen
import com.shelfie.feature.stats.ui.StatsScreen
import dagger.hilt.android.AndroidEntryPoint

private val TOP_LEVEL_ROUTES: Set<NavKey> = TopLevelDestination.entries.map { it.route }.toSet()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShelfieTheme {
                val navigationState = rememberNavigationState(
                    startRoute = TopLevelDestination.SEARCH.route,
                    topLevelRoutes = TOP_LEVEL_ROUTES
                )
                val navigator = remember(navigationState) { Navigator(navigationState) }

                // Hide bottom bar on detail screens
                val showBottomBar = navigationState.currentRoute in TOP_LEVEL_ROUTES

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            AppBottomBar(
                                currentTab = navigationState.topLevelRoute,
                                onNavigate = { dest -> navigator.navigate(dest.route) }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavDisplay(
                        entries = navigationState.toEntries(appEntryProvider(navigator)),
                        onBack = { navigator.goBack() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentTab: NavKey,
    onNavigate: (TopLevelDestination) -> Unit
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { dest ->
            val selected = currentTab == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.label
                    )
                },
                label = { Text(dest.label) }
            )
        }
    }
}

private fun appEntryProvider(navigator: Navigator): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<SearchRoute> {
        BooksScreen(onBookClick = { bookId -> navigator.navigate(BookDetailRoute(bookId)) })
    }
    entry<LibraryRoute> {
        ReadingListScreen(onBookClick = { bookId -> navigator.navigate(BookDetailRoute(bookId)) })
    }
    entry<StatsRoute> {
        StatsScreen()
    }
    entry<BookDetailRoute> { key ->
        BookDetailScreen(
            onBack = { navigator.goBack() },
            viewModel = hiltViewModel<BookDetailViewModel, BookDetailViewModel.Factory>(
                creationCallback = { factory -> factory.create(key.bookId) }
            )
        )
    }
}
