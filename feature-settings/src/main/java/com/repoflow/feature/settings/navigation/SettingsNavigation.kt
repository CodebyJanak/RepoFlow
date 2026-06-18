package com.repoflow.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.repoflow.core.navigation.Routes
import com.repoflow.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    composable(Routes.Settings.route) {
        SettingsScreen()
    }
}
