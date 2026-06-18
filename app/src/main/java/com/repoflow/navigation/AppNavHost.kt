package com.repoflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.repoflow.auth.AccountScreen
import com.repoflow.auth.LoginScreen
import com.repoflow.core.navigation.NavAnimations
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.activity.ActivityScreen
import com.repoflow.feature.home.HomeScreen
import com.repoflow.feature.repositories.RepositoriesScreen
import com.repoflow.feature.settings.SettingsScreen
import com.repoflow.feature.workspace.WorkspaceScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    isLoggedIn: Boolean?,
    modifier: Modifier = Modifier
) {
    val startDestination = when (isLoggedIn) {
        true -> Routes.Home.route
        else -> Routes.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = Routes.Login.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Login.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.Account.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Account.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.Home.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Home.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            HomeScreen(
                onNavigateToAccount = { navController.navigate(Routes.Account.route) }
            )
        }

        composable(
            route = Routes.Repositories.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Repositories.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            RepositoriesScreen()
        }

        composable(
            route = Routes.Workspace.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Workspace.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            WorkspaceScreen()
        }

        composable(
            route = Routes.Activity.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Activity.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            ActivityScreen()
        }

        composable(
            route = Routes.Settings.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Settings.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            SettingsScreen(
                onNavigateToAccount = { navController.navigate(Routes.Account.route) }
            )
        }
    }
}
