package com.repoflow.feature.workspace.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.workspace.WorkspaceScreen

fun NavGraphBuilder.workspaceGraph(navController: NavController) {
    composable(Routes.Workspace.route) {
        WorkspaceScreen()
    }
}
