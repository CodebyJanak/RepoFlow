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

    data object DiffViewer : Routes(
        "diff-viewer?localPath={localPath}&filePath={filePath}&staged={staged}",
        "https://repoflow.app/diff-viewer/{localPath}/{filePath}/{staged}"
    ) {
        fun createRoute(localPath: String, filePath: String, staged: Boolean = false): String =
            "diff-viewer?localPath=${Uri.encode(localPath)}&filePath=${Uri.encode(filePath)}&staged=$staged"
    }

    data object Issues : Routes("issues/{owner}/{name}", "https://repoflow.app/issues/{owner}/{name}") {
        fun createRoute(owner: String, name: String): String = "issues/$owner/$name"
    }

    data object IssueDetail : Routes(
        "issue/{owner}/{name}/{issueNumber}",
        "https://repoflow.app/issue/{owner}/{name}/{issueNumber}"
    ) {
        fun createRoute(owner: String, name: String, issueNumber: Int): String =
            "issue/$owner/$name/$issueNumber"
    }

    data object CreateIssue : Routes(
        "create-issue/{owner}/{name}",
        "https://repoflow.app/create-issue/{owner}/{name}"
    ) {
        fun createRoute(owner: String, name: String): String = "create-issue/$owner/$name"
    }

    data object EditIssue : Routes(
        "edit-issue/{owner}/{name}/{issueNumber}",
        "https://repoflow.app/edit-issue/{owner}/{name}/{issueNumber}"
    ) {
        fun createRoute(owner: String, name: String, issueNumber: Int): String =
            "edit-issue/$owner/$name/$issueNumber"
    }

    data object PullRequests : Routes("pull-requests/{owner}/{name}", "https://repoflow.app/pull-requests/{owner}/{name}") {
        fun createRoute(owner: String, name: String): String = "pull-requests/$owner/$name"
    }

    data object PullRequestDetail : Routes(
        "pull-request/{owner}/{name}/{pullNumber}",
        "https://repoflow.app/pull-request/{owner}/{name}/{pullNumber}"
    ) {
        fun createRoute(owner: String, name: String, pullNumber: Int): String =
            "pull-request/$owner/$name/$pullNumber"
    }

    data object CreatePullRequest : Routes(
        "create-pull-request/{owner}/{name}",
        "https://repoflow.app/create-pull-request/{owner}/{name}"
    ) {
        fun createRoute(owner: String, name: String): String = "create-pull-request/$owner/$name"
    }

    data object Actions : Routes("actions/{owner}/{name}", "https://repoflow.app/actions/{owner}/{name}") {
        fun createRoute(owner: String, name: String): String = "actions/$owner/$name"
    }

    data object ActionRunDetail : Routes(
        "action-run/{owner}/{name}/{runId}",
        "https://repoflow.app/action-run/{owner}/{name}/{runId}"
    ) {
        fun createRoute(owner: String, name: String, runId: Long): String = "action-run/$owner/$name/$runId"
    }

    data object PcBridge : Routes("pc-bridge", "https://repoflow.app/pc-bridge") {
        fun createRoute(): String = "pc-bridge"
    }

    data object PcBridgeDiscovery : Routes("pc-bridge/discovery", "https://repoflow.app/pc-bridge/discovery") {
        fun createRoute(): String = "pc-bridge/discovery"
    }

    data object PcBridgePairing : Routes(
        "pc-bridge/pairing/{host}/{port}/{deviceId}",
        "https://repoflow.app/pc-bridge/pairing/{host}/{port}/{deviceId}"
    ) {
        fun createRoute(host: String, port: Int, deviceId: String): String =
            "pc-bridge/pairing/$host/$port/$deviceId"
    }

    data object PcBridgeRemote : Routes(
        "pc-bridge/remote/{deviceId}",
        "https://repoflow.app/pc-bridge/remote/{deviceId}"
    ) {
        fun createRoute(deviceId: String): String = "pc-bridge/remote/$deviceId"
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
