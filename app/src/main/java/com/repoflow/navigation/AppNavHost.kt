package com.repoflow.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.repoflow.auth.AccountScreen
import com.repoflow.auth.LoginScreen
import com.repoflow.core.navigation.NavAnimations
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.activity.ActivityScreen
import com.repoflow.feature.commit.CommitScreen
import com.repoflow.feature.diffviewer.DiffViewerScreen
import com.repoflow.feature.home.HomeScreen
import com.repoflow.feature.gitstatus.GitStatusScreen
import com.repoflow.feature.issues.CreateIssueScreen
import com.repoflow.feature.issues.IssueDetailScreen
import com.repoflow.feature.issues.IssuesScreen
import com.repoflow.feature.pullrequests.CreatePullRequestScreen
import com.repoflow.feature.pullrequests.PullRequestDetailScreen
import com.repoflow.feature.pullrequests.PullRequestsScreen
import com.repoflow.feature.actions.ActionsDashboardScreen
import com.repoflow.feature.actions.WorkflowRunDetailScreen
import com.repoflow.feature.pcbridge.PcBridgeDiscoveryScreen
import com.repoflow.feature.pcbridge.PcBridgePairingScreen
import com.repoflow.feature.pcbridge.PcBridgeRemoteScreen
import com.repoflow.feature.pcbridge.PcBridgeScreen
import com.repoflow.feature.gitpilot.ChangelogScreen
import com.repoflow.feature.gitpilot.CommitMessageScreen
import com.repoflow.feature.gitpilot.ConflictHelpScreen
import com.repoflow.feature.gitpilot.GitErrorHelpScreen
import com.repoflow.feature.gitpilot.GitPilotScreen
import com.repoflow.feature.gitpilot.RepoSummaryScreen
import com.repoflow.feature.repositorydetail.RepositoryDetailScreen
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
            RepositoriesScreen(
                onNavigateToDetail = { owner, name ->
                    navController.navigate(Routes.RepositoryDetail.createRoute(owner, name))
                }
            )
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

        composable(
            route = Routes.RepositoryDetail.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.RepositoryDetail.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            RepositoryDetailScreen(
                owner = owner,
                name = name,
                onBack = { navController.popBackStack() },
                onNavigateToGitStatus = { localPath ->
                    navController.navigate(Routes.GitStatus.createRoute(localPath))
                },
                onNavigateToIssues = {
                    navController.navigate(Routes.Issues.createRoute(owner, name))
                },
                onNavigateToPullRequests = {
                    navController.navigate(Routes.PullRequests.createRoute(owner, name))
                },
                onNavigateToActions = {
                    navController.navigate(Routes.Actions.createRoute(owner, name))
                }
            )
        }

        composable(
            route = Routes.GitStatus.route,
            arguments = listOf(
                navArgument("localPath") { type = NavType.StringType; defaultValue = "" }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.GitStatus.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val localPath = backStackEntry.arguments?.getString("localPath")?.let {
                Uri.decode(it)
            } ?: ""
            GitStatusScreen(
                localPath = localPath,
                onBack = { navController.popBackStack() },
                onNavigateToCommit = { path ->
                    navController.navigate(Routes.Commit.createRoute(path))
                },
                onNavigateToDiff = { filePath, staged ->
                    navController.navigate(
                        Routes.DiffViewer.createRoute(localPath, filePath, staged)
                    )
                }
            )
        }

        composable(
            route = Routes.Commit.route,
            arguments = listOf(
                navArgument("localPath") { type = NavType.StringType; defaultValue = "" }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Commit.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val localPath = backStackEntry.arguments?.getString("localPath")?.let {
                Uri.decode(it)
            } ?: ""
            CommitScreen(
                localPath = localPath,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DiffViewer.route,
            arguments = listOf(
                navArgument("localPath") { type = NavType.StringType; defaultValue = "" },
                navArgument("filePath") { type = NavType.StringType; defaultValue = "" },
                navArgument("staged") { type = NavType.BoolType; defaultValue = false }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.DiffViewer.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val localPath = backStackEntry.arguments?.getString("localPath")?.let {
                Uri.decode(it)
            } ?: ""
            val filePath = backStackEntry.arguments?.getString("filePath")?.let {
                Uri.decode(it)
            } ?: ""
            val staged = backStackEntry.arguments?.getBoolean("staged") ?: false
            DiffViewerScreen(
                localPath = localPath,
                filePath = filePath,
                staged = staged,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.Issues.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Issues.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            IssuesScreen(
                owner = owner,
                name = name,
                onBack = { navController.popBackStack() },
                onIssueClick = { issueNumber ->
                    navController.navigate(Routes.IssueDetail.createRoute(owner, name, issueNumber))
                },
                onCreateIssue = {
                    navController.navigate(Routes.CreateIssue.createRoute(owner, name))
                }
            )
        }

        composable(
            route = Routes.IssueDetail.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("issueNumber") { type = NavType.IntType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.IssueDetail.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val issueNumber = backStackEntry.arguments?.getInt("issueNumber") ?: 0
            IssueDetailScreen(
                owner = owner,
                name = name,
                issueNumber = issueNumber,
                onBack = { navController.popBackStack() },
                onEdit = {
                    navController.navigate(Routes.EditIssue.createRoute(owner, name, issueNumber))
                }
            )
        }

        composable(
            route = Routes.CreateIssue.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.CreateIssue.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            CreateIssueScreen(
                owner = owner,
                name = name,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EditIssue.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("issueNumber") { type = NavType.IntType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.EditIssue.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val issueNumber = backStackEntry.arguments?.getInt("issueNumber") ?: 0
            CreateIssueScreen(
                owner = owner,
                name = name,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                editIssueNumber = issueNumber
            )
        }

        composable(
            route = Routes.PullRequests.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.PullRequests.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            PullRequestsScreen(
                owner = owner,
                name = name,
                onBack = { navController.popBackStack() },
                onPullRequestClick = { pullNumber ->
                    navController.navigate(Routes.PullRequestDetail.createRoute(owner, name, pullNumber))
                },
                onCreatePullRequest = {
                    navController.navigate(Routes.CreatePullRequest.createRoute(owner, name))
                }
            )
        }

        composable(
            route = Routes.PullRequestDetail.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("pullNumber") { type = NavType.IntType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.PullRequestDetail.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val pullNumber = backStackEntry.arguments?.getInt("pullNumber") ?: 0
            PullRequestDetailScreen(
                owner = owner,
                name = name,
                pullNumber = pullNumber,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CreatePullRequest.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.CreatePullRequest.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            CreatePullRequestScreen(
                owner = owner,
                name = name,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.Actions.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Actions.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            ActionsDashboardScreen(
                owner = owner,
                name = name,
                onBack = { navController.popBackStack() },
                onRunClick = { runId ->
                    navController.navigate(Routes.ActionRunDetail.createRoute(owner, name, runId))
                }
            )
        }

        composable(
            route = Routes.ActionRunDetail.route,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("runId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.ActionRunDetail.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val runId = backStackEntry.arguments?.getLong("runId") ?: 0L
            WorkflowRunDetailScreen(
                owner = owner,
                name = name,
                runId = runId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PcBridge.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.PcBridge.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            PcBridgeScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDiscovery = {
                    navController.navigate(Routes.PcBridgeDiscovery.createRoute())
                },
                onNavigateToPairing = { device ->
                    navController.navigate(
                        Routes.PcBridgePairing.createRoute(device.host, device.port, device.deviceId)
                    )
                },
                onNavigateToRemote = { deviceId ->
                    navController.navigate(Routes.PcBridgeRemote.createRoute(deviceId))
                }
            )
        }

        composable(
            route = Routes.PcBridgeDiscovery.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.PcBridgeDiscovery.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            PcBridgeDiscoveryScreen(
                onBack = { navController.popBackStack() },
                onDeviceSelected = { device ->
                    navController.navigate(
                        Routes.PcBridgePairing.createRoute(device.host, device.port, device.deviceId)
                    )
                }
            )
        }

        composable(
            route = Routes.PcBridgePairing.route,
            arguments = listOf(
                navArgument("host") { type = NavType.StringType },
                navArgument("port") { type = NavType.IntType },
                navArgument("deviceId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.PcBridgePairing.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) { backStackEntry ->
            val host = backStackEntry.arguments?.getString("host") ?: ""
            val port = backStackEntry.arguments?.getInt("port") ?: 0
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            val device = com.repoflow.core.domain.model.PcDevice(
                deviceId = deviceId,
                deviceName = deviceId,
                host = host,
                port = port,
                protocolVersion = "1.0.0",
                requiresPairing = true
            )
            PcBridgePairingScreen(
                device = device,
                onBack = { navController.popBackStack() },
                onPaired = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PcBridgeRemote.route,
            arguments = listOf(
                navArgument("deviceId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.PcBridgeRemote.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            PcBridgeRemoteScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GitPilot.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.GitPilot.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            GitPilotScreen(
                onBack = { navController.popBackStack() },
                onNavigateTo = { feature ->
                    val route = when (feature) {
                        "commit" -> Routes.GitPilotCommit.createRoute()
                        "changelog" -> Routes.GitPilotChangelog.createRoute()
                        "error-help" -> Routes.GitPilotErrorHelp.createRoute()
                        "conflict-help" -> Routes.GitPilotConflictHelp.createRoute()
                        "repo-summary" -> Routes.GitPilotRepoSummary.createRoute()
                        else -> return@GitPilotScreen
                    }
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = Routes.GitPilotCommit.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.GitPilotCommit.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            CommitMessageScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GitPilotChangelog.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.GitPilotChangelog.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            ChangelogScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GitPilotErrorHelp.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.GitPilotErrorHelp.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            GitErrorHelpScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GitPilotConflictHelp.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.GitPilotConflictHelp.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            ConflictHelpScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GitPilotRepoSummary.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.GitPilotRepoSummary.deepLink }
            ),
            enterTransition = NavAnimations.enterTransition,
            exitTransition = NavAnimations.exitTransition,
            popEnterTransition = NavAnimations.popEnterTransition,
            popExitTransition = NavAnimations.popExitTransition
        ) {
            RepoSummaryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
