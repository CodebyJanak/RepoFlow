package com.repoflow.feature.workspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.FeatureCard
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        RepoFlowTopAppBar(
            title = "Workspace",
            navigationIcon = null,
            onNavigationClick = null
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FeatureCard(
                    icon = Icons.Outlined.CreateNewFolder,
                    title = "New Folder",
                    description = "Create a new folder in the current workspace.",
                    onClick = {}
                )
            }

            item {
                FeatureCard(
                    icon = Icons.Outlined.UploadFile,
                    title = "Import Project",
                    description = "Import a Git project from storage or URL.",
                    onClick = {}
                )
            }

            item {
                Text(
                    text = "Recent Files",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(placeholderFiles.size) { index ->
                val file = placeholderFiles[index]
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = file.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = file.path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                EmptyState(
                    icon = Icons.Filled.Folder,
                    title = "No active workspace",
                    message = "Open a Git repository to start working.",
                    actionLabel = "Browse Files",
                    onAction = {}
                )
            }
        }
    }
}

private data class PlaceholderFile(
    val name: String,
    val path: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val placeholderFiles = listOf(
    PlaceholderFile("README.md", "/repoflow/repoflow/README.md", Icons.Filled.Description),
    PlaceholderFile("build.gradle.kts", "/repoflow/repoflow/build.gradle.kts", Icons.Filled.Description),
    PlaceholderFile("logo.png", "/repoflow/repoflow/art/logo.png", Icons.Filled.Image)
)

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun WorkspaceScreenPreview() {
    RepoFlowTheme(darkTheme = true) {
        WorkspaceScreen()
    }
}
