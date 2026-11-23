package com.ogken.safeorbit.navigation

/**
 * Type-safe navigation routes for SafeOrbit.
 */
sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Home : Screen("home")
}
