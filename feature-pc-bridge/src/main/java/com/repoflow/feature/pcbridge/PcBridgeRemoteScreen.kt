package com.repoflow.feature.pcbridge

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.domain.model.PcFileEntry
import com.repoflow.core.domain.model.PcFileStatus
import com.repoflow.core.domain.model.PcFileType
import com.repoflow.core.domain.model.PcWorkspace
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.LoadingShimmerList
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcBridgeRemoteScreen(
    onBack: () -> Unit,
    viewModel: PcBridgeViewModel = hiltViewModel()
) {
    val remoteState by viewModel.remoteState.collectAsState()
    val bridgeState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    var showCommitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(remoteState.successMessage) {
        remoteState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RepoFlowTopAppBar(
                title = bridgeState.connection.deviceName.ifEmpty { "Remote Workspace" },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { viewModel.loadGitStatus() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (remoteState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                LoadingShimmerList(count = 3, modifier = Modifier.padding(16.dp))
            }
        } else if (remoteState.workspaces.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.FolderOpen,
                    title = "No Workspaces",
                    message = "No Git workspaces are configured on the desktop agent.",
                    actionLabel = "Refresh",
                    onAction = { viewModel.loadGitStatus() }
                )
            }
        } else if (remoteState.selectedWorkspace == null) {
            WorkspaceSelector(
                workspaces = remoteState.workspaces,
                onSelect = { viewModel.selectWorkspace(it) },
                modifier = Modifier.padding(padding)
            )
        } else {
            RemoteWorkspaceContent(
                state = remoteState,
                onNavigateUp = { viewModel.navigateUp() },
                onDirectoryClick = { viewModel.navigateIntoDirectory(it) },
                onStage = { files -> viewModel.stageFiles(files) },
                onUnstage = { files -> viewModel.unstageFiles(files) },
                onCommit = { viewModel.commit(it) },
                onPush = { viewModel.push() },
                onPull = { viewModel.pull() },
                onFetch = { viewModel.fetch() },
                showCommitDialog = showCommitDialog,
                onShowCommitDialog = { showCommitDialog = true },
                onDismissCommitDialog = { showCommitDialog = false },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun WorkspaceSelector(
    workspaces: List<PcWorkspace>,
    onSelect: (PcWorkspace) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Select Workspace",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
        items(workspaces) { workspace ->
            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = MaterialTheme.shapes.medium,
                onClick = { onSelect(workspace) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = workspace.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = workspace.path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                        if (workspace.currentBranch != null) {
                            Text(
                                text = "Branch: ${workspace.currentBranch}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoteWorkspaceContent(
    state: PcRemoteUiState,
    onNavigateUp: () -> Unit,
    onDirectoryClick: (PcFileEntry) -> Unit,
    onStage: (List<String>) -> Unit,
    onUnstage: (List<String>) -> Unit,
    onCommit: (String) -> Unit,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onFetch: () -> Unit,
    showCommitDialog: Boolean,
    onShowCommitDialog: () -> Unit,
    onDismissCommitDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            WorkspaceHeader(state.selectedWorkspace!!, state.branch, state.ahead, state.behind)
        }

        item {
            GitActionBar(
                state = state,
                onPush = onPush,
                onPull = onPull,
                onFetch = onFetch,
                onShowCommitDialog = onShowCommitDialog
            )
        }

        if (state.conflicts.isNotEmpty()) {
            item {
                ConflictWarning(conflicts = state.conflicts)
            }
        }

        item {
            GitStatusSection(
                stagedFiles = state.stagedFiles,
                unstagedFiles = state.unstagedFiles,
                untrackedFiles = state.untrackedFiles,
                onStage = onStage,
                onUnstage = onUnstage,
                isGitLoading = state.isGitLoading
            )
        }

        item {
            FileBrowserSection(
                currentDirectory = state.currentDirectory,
                entries = state.directoryEntries,
                isLoading = state.isDirectoryLoading,
                onNavigateUp = onNavigateUp,
                onEntryClick = onDirectoryClick
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showCommitDialog) {
        CommitDialog(
            onCommit = { msg ->
                onCommit(msg)
                onDismissCommitDialog()
            },
            onDismiss = onDismissCommitDialog
        )
    }
}

@Composable
private fun WorkspaceHeader(workspace: PcWorkspace, branch: String, ahead: Int = 0, behind: Int = 0) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = workspace.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = workspace.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
            if (branch.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = branch,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                    if (ahead > 0 || behind > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "↑$ahead ↓$behind",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GitActionBar(
    state: PcRemoteUiState,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onFetch: () -> Unit,
    onShowCommitDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GitActionButton(
                icon = Icons.Outlined.Commit,
                label = "Commit",
                enabled = state.stagedFiles.isNotEmpty(),
                onClick = onShowCommitDialog
            )
            GitActionButton(
                icon = Icons.Outlined.CloudUpload,
                label = "Push",
                enabled = state.ahead > 0 && state.selectedWorkspace != null,
                onClick = onPush
            )
            GitActionButton(
                icon = Icons.Outlined.CloudDownload,
                label = "Pull",
                enabled = state.selectedWorkspace != null,
                onClick = onPull
            )
            GitActionButton(
                icon = Icons.Filled.Refresh,
                label = "Fetch",
                enabled = state.selectedWorkspace != null,
                onClick = onFetch
            )
        }
    }
}

@Composable
private fun GitActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled) { onClick() }.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun ConflictWarning(conflicts: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Merge Conflicts",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                conflicts.forEach { conflict ->
                    Text(
                        text = conflict,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun GitStatusSection(
    stagedFiles: List<PcFileStatus>,
    unstagedFiles: List<PcFileStatus>,
    untrackedFiles: List<String>,
    onStage: (List<String>) -> Unit,
    onUnstage: (List<String>) -> Unit,
    isGitLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Git Status",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isGitLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp).align(Alignment.CenterHorizontally))
            } else {
                var selectedStaged by remember { mutableStateOf(setOf<String>()) }
                var selectedUnstaged by remember { mutableStateOf(setOf<String>()) }

                if (stagedFiles.isNotEmpty()) {
                    Text(
                        text = "Staged (${stagedFiles.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    stagedFiles.forEach { file ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedStaged = if (file.path in selectedStaged)
                                    selectedStaged - file.path
                                else
                                    selectedStaged + file.path
                            }.padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = file.path in selectedStaged,
                                onCheckedChange = {
                                    selectedStaged = if (file.path in selectedStaged)
                                        selectedStaged - file.path
                                    else
                                        selectedStaged + file.path
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FileStatusLabel(file.status)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = file.path,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (selectedStaged.isNotEmpty()) {
                        Button(
                            onClick = { onUnstage(selectedStaged.toList()); selectedStaged = emptySet() },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Unstage Selected (${selectedStaged.size})")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (unstagedFiles.isNotEmpty()) {
                    Text(
                        text = "Unstaged (${unstagedFiles.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    unstagedFiles.forEach { file ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedUnstaged = if (file.path in selectedUnstaged)
                                    selectedUnstaged - file.path
                                else
                                    selectedUnstaged + file.path
                            }.padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = file.path in selectedUnstaged,
                                onCheckedChange = {
                                    selectedUnstaged = if (file.path in selectedUnstaged)
                                        selectedUnstaged - file.path
                                    else
                                        selectedUnstaged + file.path
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FileStatusLabel(file.status)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = file.path,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (selectedUnstaged.isNotEmpty()) {
                        Button(
                            onClick = { onStage(selectedUnstaged.toList()); selectedUnstaged = emptySet() },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Stage Selected (${selectedUnstaged.size})")
                        }
                    }
                }

                if (untrackedFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Untracked (${untrackedFiles.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    untrackedFiles.forEach { file ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = file,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (stagedFiles.isEmpty() && unstagedFiles.isEmpty() && untrackedFiles.isEmpty()) {
                    Text(
                        text = "Working tree clean",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FileStatusLabel(status: String) {
    val (color, label) = when (status.lowercase()) {
        "modified" -> MaterialTheme.colorScheme.primary to "M"
        "added" -> MaterialTheme.colorScheme.tertiary to "A"
        "deleted" -> MaterialTheme.colorScheme.error to "D"
        "renamed" -> MaterialTheme.colorScheme.secondary to "R"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to "?"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace
        ),
        color = color
    )
}

@Composable
private fun FileBrowserSection(
    currentDirectory: String,
    entries: List<PcFileEntry>,
    isLoading: Boolean,
    onNavigateUp: () -> Unit,
    onEntryClick: (PcFileEntry) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Files",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (currentDirectory.isNotEmpty()) {
                    TextButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Up", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Up", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentDirectory,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp).align(Alignment.CenterHorizontally))
            } else if (entries.isEmpty()) {
                Text(
                    text = "Empty directory",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEntryClick(entry) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (entry.type == PcFileType.DIRECTORY)
                                Icons.Filled.Folder else Icons.Filled.Description,
                            contentDescription = null,
                            tint = if (entry.type == PcFileType.DIRECTORY)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (entry.size != null && entry.type == PcFileType.FILE) {
                            Text(
                                text = formatFileSize(entry.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitDialog(
    onCommit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var commitMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Commit, contentDescription = null) },
        title = { Text("Commit Changes") },
        text = {
            OutlinedTextField(
                value = commitMessage,
                onValueChange = { commitMessage = it },
                label = { Text("Commit message") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onCommit(commitMessage) },
                enabled = commitMessage.isNotBlank()
            ) {
                Text("Commit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
    bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}
