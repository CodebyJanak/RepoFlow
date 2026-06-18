package com.repoflow.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    companion object {
        val items = listOf(
            BottomNavItem(
                route = Routes.Home.route,
                title = "Home",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home
            ),
            BottomNavItem(
                route = Routes.Repositories.route,
                title = "Repositories",
                icon = Icons.Outlined.Code,
                selectedIcon = Icons.Filled.Code
            ),
            BottomNavItem(
                route = Routes.Workspace.route,
                title = "Workspace",
                icon = Icons.Outlined.Folder,
                selectedIcon = Icons.Filled.Folder
            ),
            BottomNavItem(
                route = Routes.Activity.route,
                title = "Activity",
                icon = Icons.Outlined.Timeline,
                selectedIcon = Icons.Filled.Timeline
            ),
            BottomNavItem(
                route = Routes.Settings.route,
                title = "Settings",
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings
            )
        )
    }
}
