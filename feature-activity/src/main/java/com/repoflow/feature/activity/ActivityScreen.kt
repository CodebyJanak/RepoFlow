package com.repoflow.feature.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowUpward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.domain.model.ActivityItem
import com.repoflow.core.domain.model.ActivityType
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.RepoFlowTopAppBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val filteredActivities by viewModel.filteredActivities.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val groupedActivities = remember(filteredActivities) {
        groupByDate(filteredActivities)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        RepoFlowTopAppBar(
            title = "Activity",
            navigationIcon = null,
            onNavigationClick = null,
            scrollBehavior = scrollBehavior,
            actions = {
                AnimatedVisibility(visible = filteredActivities.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearActivities() }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear all",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        FilterChipRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setFilter(it) }
        )

        if (isEmpty) {
            EmptyState(
                icon = Icons.Outlined.Inbox,
                title = "No activity",
                message = when (selectedFilter) {
                    ActivityFilter.ALL -> "Your Git activity will appear here as you work."
                    ActivityFilter.COMMIT -> "No commits yet. Create your first commit to see it here."
                    ActivityFilter.PUSH -> "No pushes yet. Push your changes to see them here."
                    ActivityFilter.PULL -> "No pulls yet. Pull changes to see them here."
                    ActivityFilter.FETCH -> "No fetches yet. Fetch updates to see them here."
                    ActivityFilter.SYNC -> "No sync activity yet. Sync your repositories to see them here."
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ActivityTimeline(
                groupedActivities = groupedActivities,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    selectedFilter: ActivityFilter,
    onFilterSelected: (ActivityFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActivityFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = selectedFilter == filter
                )
            )
        }
    }
}

@Composable
private fun ActivityTimeline(
    groupedActivities: Map<String, List<ActivityItem>>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groupedActivities.entries.forEach { (dateLabel, activities) ->
            item(key = "header_$dateLabel") {
                DateHeader(label = dateLabel)
            }

            items(
                items = activities,
                key = { it.id }
            ) { activity ->
                TimelineCard(activity = activity)
            }

            item(key = "spacer_$dateLabel") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.Center,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "All caught up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateHeader(
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun TimelineCard(
    activity: ActivityItem,
    modifier: Modifier = Modifier
) {
    val iconColor = iconColorForType(activity.type)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.type.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = activity.repositoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = formatRelativeTime(activity.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun groupByDate(activities: List<ActivityItem>): Map<String, List<ActivityItem>> {
    if (activities.isEmpty()) return emptyMap()

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L

    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    return activities.groupBy { item ->
        when {
            item.timestamp >= todayStart -> "Today"
            item.timestamp >= yesterdayStart -> "Yesterday"
            else -> dateFormat.format(Date(item.timestamp))
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000L}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
        diff < 172_800_000L -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private val ActivityFilter.icon: ImageVector
    get() = when (this) {
        ActivityFilter.ALL -> Icons.Filled.Sync
        ActivityFilter.PUSH -> Icons.AutoMirrored.Filled.ArrowUpward
        ActivityFilter.COMMIT -> Icons.Filled.Commit
        ActivityFilter.PULL -> Icons.Filled.CallMerge
        ActivityFilter.FETCH -> Icons.Filled.Refresh
        ActivityFilter.SYNC -> Icons.Filled.Sync
    }

private val ActivityType.icon: ImageVector
    get() = when (this) {
        ActivityType.COMMIT -> Icons.Filled.Commit
        ActivityType.PUSH -> Icons.AutoMirrored.Filled.ArrowUpward
        ActivityType.PULL -> Icons.Filled.CallMerge
        ActivityType.FETCH -> Icons.Filled.Refresh
        ActivityType.CLONE -> Icons.Filled.ContentCopy
        ActivityType.BRANCH_SWITCH -> Icons.Filled.AccountTree
        ActivityType.BRANCH_CREATE -> Icons.Filled.Add
        ActivityType.BRANCH_DELETE -> Icons.Filled.Delete
        ActivityType.MERGE -> Icons.Filled.CallMerge
        ActivityType.ERROR -> Icons.Filled.Error
    }

@Composable
private fun iconColorForType(type: ActivityType): Color = when (type) {
    ActivityType.COMMIT -> MaterialTheme.colorScheme.primary
    ActivityType.PUSH -> MaterialTheme.colorScheme.tertiary
    ActivityType.PULL -> MaterialTheme.colorScheme.secondary
    ActivityType.FETCH -> MaterialTheme.colorScheme.tertiary
    ActivityType.CLONE -> MaterialTheme.colorScheme.primary
    ActivityType.BRANCH_SWITCH -> MaterialTheme.colorScheme.secondary
    ActivityType.BRANCH_CREATE -> MaterialTheme.colorScheme.primary
    ActivityType.BRANCH_DELETE -> MaterialTheme.colorScheme.error
    ActivityType.MERGE -> MaterialTheme.colorScheme.secondary
    ActivityType.ERROR -> MaterialTheme.colorScheme.error
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun ActivityScreenPreview() {
    RepoFlowTheme(darkTheme = true) {
        ActivityScreen()
    }
}
