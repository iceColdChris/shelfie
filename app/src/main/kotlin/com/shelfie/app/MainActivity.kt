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
import com.shelfie.app.navigation.BookDetailNavKey
import com.shelfie.app.navigation.BooksNavKey
import com.shelfie.app.navigation.Navigator
import com.shelfie.app.navigation.ReadingListNavKey
import com.shelfie.app.navigation.StatsNavKey
import com.shelfie.app.navigation.TopLevelNavItem
import com.shelfie.app.navigation.rememberNavigationState
import com.shelfie.app.navigation.toEntries
import com.shelfie.core.designsystem.theme.ShelfieTheme
import com.shelfie.feature.books.BookDetailScreen
import com.shelfie.feature.books.BookDetailViewModel
import com.shelfie.feature.books.BooksScreen
import com.shelfie.feature.readinglist.ReadingListScreen
import com.shelfie.feature.stats.StatsScreen
import dagger.hilt.android.AndroidEntryPoint

private val TOP_LEVEL_KEYS: Set<NavKey> = TopLevelNavItem.entries.map { it.navKey }.toSet()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShelfieTheme {
                val navigationState = rememberNavigationState(
                    startKey = TopLevelNavItem.SEARCH.navKey,
                    topLevelKeys = TOP_LEVEL_KEYS
                )
                val navigator = remember(navigationState) { Navigator(navigationState) }

                // Hide bottom bar on detail screens
                val showBottomBar = navigationState.currentKey in TOP_LEVEL_KEYS

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            AppBottomBar(
                                currentTab = navigationState.topLevelKey,
                                onNavigate = { dest -> navigator.navigate(dest.navKey) }
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
    onNavigate: (TopLevelNavItem) -> Unit
) {
    NavigationBar {
        TopLevelNavItem.entries.forEach { dest ->
            val selected = currentTab == dest.navKey
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
    entry<BooksNavKey> {
        BooksScreen(onBookClick = { bookId -> navigator.navigate(BookDetailNavKey(bookId)) })
    }
    entry<ReadingListNavKey> {
        ReadingListScreen(onBookClick = { bookId -> navigator.navigate(BookDetailNavKey(bookId)) })
    }
    entry<StatsNavKey> {
        StatsScreen()
    }
    entry<BookDetailNavKey> { key ->
        BookDetailScreen(
            onBack = { navigator.goBack() },
            viewModel = hiltViewModel<BookDetailViewModel, BookDetailViewModel.Factory>(
                creationCallback = { factory -> factory.create(key.bookId) }
            )
        )
    }
}
