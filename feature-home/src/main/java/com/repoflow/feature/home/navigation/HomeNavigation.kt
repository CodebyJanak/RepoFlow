package com.repoflow.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.home.HomeScreen

fun NavGraphBuilder.homeGraph(navController: NavController) {
    composable(Routes.Home.route) {
        HomeScreen()
    }
}
