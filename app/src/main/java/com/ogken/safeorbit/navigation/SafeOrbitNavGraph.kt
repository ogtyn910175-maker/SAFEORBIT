package com.ogken.safeorbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ogken.safeorbit.ui.auth.AuthScreen
import com.ogken.safeorbit.ui.circle.CircleMembersScreen
import com.ogken.safeorbit.ui.circle.CreateCircleScreen
import com.ogken.safeorbit.ui.circle.JoinCircleScreen
import com.ogken.safeorbit.ui.home.HomeScreen
import com.ogken.safeorbit.ui.profile.ProfileSettingsScreen
import com.ogken.safeorbit.ui.profile.ProfileSetupScreen
import com.ogken.safeorbit.ui.settings.SettingsScreen
import com.ogken.safeorbit.ui.welcome.WelcomeScreen

private fun isUserLoggedIn(): Boolean {
    return try {
        FirebaseAuth.getInstance().currentUser != null
    } catch (_: Exception) {
        false
    }
}

@Composable
fun SafeOrbitNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = if (isUserLoggedIn()) Screen.Home.route else Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onContinueClick = {
                    val destination = if (isUserLoggedIn()) Screen.Home.route else Screen.Auth.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSetupProfileClick = {
                    navController.navigate(Screen.Auth.route)
                }
            )
        }

        composable(route = Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onProfileSaved = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ProfileSettings.route) {
            ProfileSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToCircleMembers = { navController.navigate(Screen.CircleMembers.route) },
                onNavigateToProfile = { navController.navigate(Screen.ProfileSettings.route) },
                onNavigateToCreateCircle = { navController.navigate(Screen.CreateCircle.route) },
                onNavigateToJoinCircle = { navController.navigate(Screen.JoinCircle.route) }
            )
        }

        composable(route = Screen.CreateCircle.route) {
            CreateCircleScreen(
                onNavigateBack = { navController.popBackStack() },
                onCircleCreated = {
                    navController.navigate(Screen.CircleMembers.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(route = Screen.JoinCircle.route) {
            JoinCircleScreen(
                onNavigateBack = { navController.popBackStack() },
                onCircleJoined = {
                    navController.navigate(Screen.CircleMembers.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(route = Screen.CircleMembers.route) {
            CircleMembersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateCreateCircle = { navController.navigate(Screen.CreateCircle.route) },
                onNavigateJoinCircle = { navController.navigate(Screen.JoinCircle.route) }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateProfileSettings = { navController.navigate(Screen.ProfileSettings.route) },
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
