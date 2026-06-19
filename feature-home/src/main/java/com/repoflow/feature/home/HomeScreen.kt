package com.repoflow.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.FeatureCard
import com.repoflow.core.ui.components.RepoFlowTopAppBar
import com.repoflow.core.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAccount: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        RepoFlowTopAppBar(
            title = "RepoFlow",
            navigationIcon = null,
            onNavigationClick = null,
            actions = {
                IconButton(onClick = onNavigateToAccount) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Account",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    onSearch = {},
                    placeholder = "Search repositories..."
                )
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                FeatureCard(
                    icon = Icons.Outlined.CloudSync,
                    title = "Sync Repositories",
                    description = "Pull latest changes from all your repositories.",
                    onClick = {}
                )
            }

            item {
                FeatureCard(
                    icon = Icons.Outlined.Commit,
                    title = "Quick Commit",
                    description = "Stage and commit changes with a single tap.",
                    onClick = {}
                )
            }

            item {
                FeatureCard(
                    icon = Icons.Outlined.Computer,
                    title = "Remote Workspace",
                    description = "Connect to a remote PC workspace.",
                    onClick = {}
                )
            }

            item {
                Text(
                    text = "Recent Repositories",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                EmptyState(
                    icon = Icons.Outlined.Storage,
                    title = "No repositories yet",
                    message = "Login with GitHub or create a new repository to get started.",
                    actionLabel = "New Repository",
                    onAction = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun HomeScreenPreview() {
    RepoFlowTheme(darkTheme = true) {
        HomeScreen()
    }
}
