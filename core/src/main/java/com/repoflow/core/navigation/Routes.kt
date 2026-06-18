package com.repoflow.core.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Repositories : Routes("repositories")
    data object Workspace : Routes("workspace")
    data object Activity : Routes("activity")
    data object Settings : Routes("settings")
}
