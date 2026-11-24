package com.shelfie.app.navigation

import androidx.navigation3.runtime.NavKey

/** Turns navigation events into changes to [NavigationState]. */
class Navigator(private val state: NavigationState) {

    fun navigate(key: NavKey) {
        if (key in state.backStacks.keys) {
            // A tab root: switch tabs and keep that tab's history.
            state.topLevelKey = key
        } else {
            state.backStacks.getValue(state.topLevelKey).add(key)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks.getValue(state.topLevelKey)
        // At a tab's root, back returns to the start tab instead of leaving the app.
        if (currentStack.last() == state.topLevelKey) {
            state.topLevelKey = state.startKey
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
