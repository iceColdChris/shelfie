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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shelfie.core.ui.theme.ShelfieTheme
import com.shelfie.feature.books.ui.BookDetailScreen
import com.shelfie.feature.books.ui.BooksScreen
import com.shelfie.feature.readinglist.ui.ReadingListScreen
import com.shelfie.feature.stats.ui.StatsScreen
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShelfieTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                // Hide bottom bar on detail screens
                val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            AppBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { dest ->
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (TopLevelDestination) -> Unit
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { dest ->
            val selected = currentRoute == dest.route
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

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.SEARCH.route,
        modifier = modifier
    ) {
        // -- Search tab --
        composable(TopLevelDestination.SEARCH.route) {
            BooksScreen(
                onBookClick = { bookId ->
                    val encoded = URLEncoder.encode(bookId, "UTF-8")
                    navController.navigate("bookDetail/$encoded")
                }
            )
        }

        composable(
            route = "bookDetail/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) {
            BookDetailScreen(onBack = { navController.popBackStack() })
        }

        // -- Library tab --
        composable(TopLevelDestination.LIBRARY.route) {
            ReadingListScreen(
                onBookClick = { bookId ->
                    val encoded = URLEncoder.encode(bookId, "UTF-8")
                    navController.navigate("bookDetail/$encoded")
                }
            )
        }

        // -- Stats tab --
        composable(TopLevelDestination.STATS.route) {
            StatsScreen()
        }
    }
}
