package com.repoflow.feature.activity

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        RepoFlowTopAppBar(
            title = "Activity",
            navigationIcon = null,
            onNavigationClick = null
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            itemsIndexed(placeholderActivities) { index, activity ->
                ActivityItem(activity = activity)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                EmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "No more activity",
                    message = "Your Git activity will appear here as you work.",
                    actionLabel = "View All",
                    onAction = {}
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: PlaceholderActivity) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = activity.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = activity.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private data class PlaceholderActivity(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val timestamp: String
)

private val placeholderActivities = listOf(
    PlaceholderActivity(
        icon = Icons.AutoMirrored.Filled.ArrowUpward,
        title = "Pushed to main",
        description = "repoflow/repoflow · 3 commits",
        timestamp = "2 minutes ago"
    ),
    PlaceholderActivity(
        icon = Icons.Filled.Commit,
        title = "Commit created",
        description = "feat: add dark mode support",
        timestamp = "15 minutes ago"
    ),
    PlaceholderActivity(
        icon = Icons.Filled.CallMerge,
        title = "Branch merged",
        description = "feature/nav → develop",
        timestamp = "1 hour ago"
    ),
    PlaceholderActivity(
        icon = Icons.Filled.Build,
        title = "Build completed",
        description = "CI #142 — main branch",
        timestamp = "2 hours ago"
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun ActivityScreenPreview() {
    RepoFlowTheme(darkTheme = true) {
        ActivityScreen()
    }
}
