package com.repoflow.feature.repositories.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.repositories.RepositoriesScreen

fun NavGraphBuilder.repositoriesGraph(navController: NavController) {
    composable(Routes.Repositories.route) {
        RepositoriesScreen()
    }
}
