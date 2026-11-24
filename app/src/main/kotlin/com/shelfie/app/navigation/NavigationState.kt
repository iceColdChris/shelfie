package com.shelfie.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

// Adapted from the Navigation 3 migration guide (nav3-recipes 1.2.0): one back stack per
// top-level tab, so switching tabs keeps each tab's history, as saveState/restoreState did.

/** Creates navigation state that survives configuration changes and process death. */
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>
): NavigationState {
    val topLevelKey = rememberSerializable(
        startKey, topLevelKeys,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startKey)
    }

    val backStacks = topLevelKeys.associateWith { key -> rememberNavBackStack(key) }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelKey = topLevelKey,
            backStacks = backStacks
        )
    }
}

/**
 * @param startKey the tab the user exits the app from.
 * @param topLevelKey the tab currently shown.
 * @param backStacks one back stack per tab, keyed by the tab's root key.
 */
class NavigationState(
    val startKey: NavKey,
    topLevelKey: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {
    var topLevelKey: NavKey by topLevelKey

    val stacksInUse: List<NavKey>
        get() = if (topLevelKey == startKey) {
            listOf(startKey)
        } else {
            listOf(startKey, topLevelKey)
        }

    /** The key on top of the visible tab's back stack. */
    val currentKey: NavKey?
        get() = backStacks[topLevelKey]?.lastOrNull()
}

/** Converts the state into decorated entries for NavDisplay. */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): List<NavEntry<NavKey>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                // Scopes each ViewModel to its entry, so every BookDetailNavKey gets its own.
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider
        )
    }

    return stacksInUse.flatMap { decoratedEntries[it].orEmpty() }
}
