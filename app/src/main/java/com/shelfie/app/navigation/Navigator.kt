package com.shelfie.app.navigation

import androidx.navigation3.runtime.NavKey

/** Turns navigation events into changes to [NavigationState]. */
class Navigator(private val state: NavigationState) {

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // A tab root: switch tabs and keep that tab's history.
            state.topLevelRoute = route
        } else {
            state.backStacks.getValue(state.topLevelRoute).add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks.getValue(state.topLevelRoute)
        // At a tab's root, back returns to the start tab instead of leaving the app.
        if (currentStack.last() == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
