package com.repoflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.activity.navigation.activityGraph
import com.repoflow.feature.home.navigation.homeGraph
import com.repoflow.feature.repositories.navigation.repositoriesGraph
import com.repoflow.feature.settings.navigation.settingsGraph
import com.repoflow.feature.workspace.navigation.workspaceGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        homeGraph(navController)
        repositoriesGraph(navController)
        workspaceGraph(navController)
        activityGraph(navController)
        settingsGraph(navController)
    }
}
