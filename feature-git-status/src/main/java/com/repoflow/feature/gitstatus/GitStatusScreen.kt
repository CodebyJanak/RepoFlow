package com.repoflow.feature.gitstatus

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HorizontalSplit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.domain.model.FileStatusType
import com.repoflow.core.domain.model.StatusFile
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.LoadingShimmerList
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitStatusScreen(
    localPath: String,
    onBack: () -> Unit,
    onNavigateToCommit: (String) -> Unit = {},
    onNavigateToDiff: (String, Boolean) -> Unit = { _, _ -> },
    viewModel: GitStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(localPath) {
        viewModel.loadStatus(localPath)
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            RepoFlowTopAppBar(
                title = "Git Status",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
                actions = {
                    TextButton(onClick = { onNavigateToCommit(localPath) }) {
                        Text("Commit")
                    }
                    IconButton(onClick = { viewModel.loadStatus(localPath) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingShimmerList(
                    count = 6,
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            state.modifiedFiles.isEmpty() &&
            state.newFiles.isEmpty() &&
            state.deletedFiles.isEmpty() &&
            state.conflictingFiles.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.CheckCircle,
                    title = "Clean working tree",
                    message = "No uncommitted changes detected.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            else -> {
                GitStatusContent(
                    state = state,
                    onStageFile = viewModel::stageFile,
                    onUnstageFile = viewModel::unstageFile,
                    onStageAll = viewModel::stageAll,
                    onUnstageAll = viewModel::unstageAll,
                    onNavigateToDiff = onNavigateToDiff,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun GitStatusContent(
    state: GitStatusUiState,
    onStageFile: (String) -> Unit,
    onUnstageFile: (String) -> Unit,
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    onNavigateToDiff: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                stagedCount = state.stagedCount,
                unstagedCount = state.unstagedCount,
                modifiedCount = state.modifiedFiles.size,
                addedCount = state.newFiles.size,
                deletedCount = state.deletedFiles.size,
                conflictingCount = state.conflictingFiles.size
            )
        }

        item {
            StagingActions(
                onStageAll = onStageAll,
                onUnstageAll = onUnstageAll,
                isStageAllLoading = state.isStageAllLoading,
                isUnstageAllLoading = state.isUnstageAllLoading,
                hasFiles = state.stagedCount > 0 || state.unstagedCount > 0
            )
        }

        if (state.conflictingFiles.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.Warning,
                    title = "Conflicts",
                    count = state.conflictingFiles.size,
                    color = MaterialTheme.colorScheme.error
                )
            }
            items(state.conflictingFiles, key = { it.path }) { file ->
                StatusFileRow(
                    file = file,
                    onClick = { onNavigateToDiff(file.path, file.staged) },
                    onStageToggle = {
                        if (file.staged) onUnstageFile(file.path) else onStageFile(file.path)
                    },
                    accentColor = MaterialTheme.colorScheme.error
                )
            }
        }

        if (state.modifiedFiles.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.Edit,
                    title = "Modified",
                    count = state.modifiedFiles.size,
                    color = statusColor(FileStatusType.MODIFIED)
                )
            }
            items(state.modifiedFiles, key = { it.path }) { file ->
                StatusFileRow(
                    file = file,
                    onClick = { onNavigateToDiff(file.path, file.staged) },
                    onStageToggle = {
                        if (file.staged) onUnstageFile(file.path) else onStageFile(file.path)
                    },
                    accentColor = statusColor(FileStatusType.MODIFIED)
                )
            }
        }

        if (state.newFiles.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.AddCircleOutline,
                    title = "New",
                    count = state.newFiles.size,
                    color = statusColor(FileStatusType.ADDED)
                )
            }
            items(state.newFiles, key = { it.path }) { file ->
                StatusFileRow(
                    file = file,
                    onClick = { onNavigateToDiff(file.path, file.staged) },
                    onStageToggle = {
                        if (file.staged) onUnstageFile(file.path) else onStageFile(file.path)
                    },
                    accentColor = statusColor(FileStatusType.ADDED)
                )
            }
        }

        if (state.deletedFiles.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.DeleteOutline,
                    title = "Deleted",
                    count = state.deletedFiles.size,
                    color = statusColor(FileStatusType.DELETED)
                )
            }
            items(state.deletedFiles, key = { it.path }) { file ->
                StatusFileRow(
                    file = file,
                    onClick = { onNavigateToDiff(file.path, file.staged) },
                    onStageToggle = {
                        if (file.staged) onUnstageFile(file.path) else onStageFile(file.path)
                    },
                    accentColor = statusColor(FileStatusType.DELETED)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    stagedCount: Int,
    unstagedCount: Int,
    modifiedCount: Int,
    addedCount: Int,
    deletedCount: Int,
    conflictingCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(350)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(count = modifiedCount, label = "Modified", color = statusColor(FileStatusType.MODIFIED))
                SummaryItem(count = addedCount, label = "Added", color = statusColor(FileStatusType.ADDED))
                SummaryItem(count = deletedCount, label = "Deleted", color = statusColor(FileStatusType.DELETED))
                if (conflictingCount > 0) {
                    SummaryItem(count = conflictingCount, label = "Conflicts", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "$stagedCount staged",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$unstagedCount unstaged",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StagingActions(
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    isStageAllLoading: Boolean,
    isUnstageAllLoading: Boolean,
    hasFiles: Boolean
) {
    if (!hasFiles) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onStageAll,
            enabled = !isStageAllLoading && !isUnstageAllLoading,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isStageAllLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Filled.Inbox, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stage All")
            }
        }

        Button(
            onClick = onUnstageAll,
            enabled = !isStageAllLoading && !isUnstageAllLoading,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isUnstageAllLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Icon(Icons.Outlined.SelectAll, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unstage All")
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFileRow(
    file: StatusFile,
    onClick: () -> Unit,
    onStageToggle: () -> Unit,
    accentColor: Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(200)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onStageToggle,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = if (file.staged) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (file.staged) "Staged" else "Unstaged",
                    tint = if (file.staged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = statusIcon(file.status),
                contentDescription = file.status.name,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = file.path,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.width(8.dp))

            Badge(
                text = if (file.staged) "Staged" else "Unstaged",
                color = if (file.staged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun statusColor(type: FileStatusType): Color {
    return when (type) {
        FileStatusType.MODIFIED -> Color(0xFFFFA726)
        FileStatusType.ADDED -> Color(0xFF66BB6A)
        FileStatusType.DELETED -> Color(0xFFEF5350)
        FileStatusType.CONFLICTING -> Color(0xFFE53935)
    }
}

private fun statusIcon(type: FileStatusType): ImageVector {
    return when (type) {
        FileStatusType.MODIFIED -> Icons.Filled.Edit
        FileStatusType.ADDED -> Icons.Filled.AddCircleOutline
        FileStatusType.DELETED -> Icons.Filled.DeleteOutline
        FileStatusType.CONFLICTING -> Icons.Filled.Warning
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun GitStatusScreenPreview() {
    RepoFlowTheme(darkTheme = true) {
        GitStatusScreen(
            localPath = "/tmp/test-repo",
            onBack = {}
        )
    }
}
