package com.ogken.safeorbit.navigation

/**
 * Type-safe navigation routes for SafeOrbit Stage 2.
 */
sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object ProfileSetup : Screen("profile_setup")
    data object ProfileSettings : Screen("profile_settings")
    data object CreateCircle : Screen("create_circle")
    data object JoinCircle : Screen("join_circle")
    data object CircleMembers : Screen("circle_members")
    data object Settings : Screen("settings")
}
