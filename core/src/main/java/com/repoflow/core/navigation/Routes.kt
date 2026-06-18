package com.repoflow.core.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

sealed class Routes(
    val route: String,
    val deepLink: String = "https://repoflow.app/$route"
) {
    data object Home : Routes("home", "https://repoflow.app/home")
    data object Repositories : Routes("repositories", "https://repoflow.app/repositories")
    data object Workspace : Routes("workspace", "https://repoflow.app/workspace")
    data object Activity : Routes("activity", "https://repoflow.app/activity")
    data object Settings : Routes("settings", "https://repoflow.app/settings")
    data object Login : Routes("login", "https://repoflow.app/login")
    data object Account : Routes("account", "https://repoflow.app/account")
    data object RepositoryDetail : Routes("repository/{owner}/{name}", "https://repoflow.app/repository/{owner}/{name}") {
        fun createRoute(owner: String, name: String): String = "repository/$owner/$name"
    }

    data object GitStatus : Routes("git-status?localPath={localPath}", "https://repoflow.app/git-status/{localPath}") {
        fun createRoute(localPath: String): String = "git-status?localPath=${Uri.encode(localPath)}"
    }

    data object Commit : Routes("commit?localPath={localPath}", "https://repoflow.app/commit/{localPath}") {
        fun createRoute(localPath: String): String = "commit?localPath=${Uri.encode(localPath)}"
    }

    companion object {
        val bottomNavRoutes = listOf(Home, Repositories, Workspace, Activity, Settings)

        fun fromRoute(route: String?): Routes? = when (route) {
            Home.route -> Home
            Repositories.route -> Repositories
            Workspace.route -> Workspace
            Activity.route -> Activity
            Settings.route -> Settings
            else -> null
        }
    }
}

object NavAnimations {
    private const val duration = 300

    val enterTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.EnterTransition = {
        fadeIn(animationSpec = tween(duration)) +
            slideInHorizontally(
                animationSpec = tween(duration),
                initialOffsetX = { it / 4 }
            )
    }

    val exitTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.ExitTransition = {
        fadeOut(animationSpec = tween(duration))
    }

    val popEnterTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.EnterTransition = {
        fadeIn(animationSpec = tween(duration))
    }

    val popExitTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.ExitTransition = {
        fadeOut(animationSpec = tween(duration)) +
            slideOutHorizontally(
                animationSpec = tween(duration),
                targetOffsetX = { it / 4 }
            )
    }
}
