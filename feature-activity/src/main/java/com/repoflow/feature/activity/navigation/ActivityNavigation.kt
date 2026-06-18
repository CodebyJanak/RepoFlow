package com.repoflow.feature.activity.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.activity.ActivityScreen

fun NavGraphBuilder.activityGraph(navController: NavController) {
    composable(Routes.Activity.route) {
        ActivityScreen()
    }
}
