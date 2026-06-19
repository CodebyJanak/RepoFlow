package com.repoflow.feature.workspace

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.domain.model.WorkspaceFile
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.LoadingShimmerList
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showSearch by remember { mutableStateOf(false) }
    var searchQueryText by remember { mutableStateOf("") }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            viewModel.onRootUriSelected(it.toString())
        }
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchTopBar(
                    query = searchQueryText,
                    onQueryChange = {
                        searchQueryText = it
                        viewModel.search(it)
                    },
                    onClose = {
                        showSearch = false
                        searchQueryText = ""
                        viewModel.clearSearch()
                    }
                )
            } else {
                RepoFlowTopAppBar(
                    title = "Workspace",
                    navigationIcon = null,
                    onNavigationClick = null,
                    actions = {
                        if (uiState.rootUri != null) {
                            IconButton(onClick = { showSearch = true }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { showCreateFolderDialog = true }) {
                                Icon(Icons.Filled.CreateNewFolder, contentDescription = "New folder")
                            }
                        }
                        IconButton(onClick = { folderPickerLauncher.launch(null) }) {
                            Icon(Icons.Outlined.FolderOpen, contentDescription = "Browse")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.rootUri != null && !showSearch) {
                BreadcrumbBar(
                    breadcrumbs = uiState.folderStack,
                    onBreadcrumbClick = { index -> viewModel.navigateToBreadcrumb(index) },
                    onNavigateUp = { viewModel.navigateUp() },
                    isGitRepository = uiState.isGitRepository
                )
            }

            when {
                uiState.rootUri == null -> {
                    EmptyState(
                        icon = Icons.Outlined.FolderOpen,
                        title = "No workspace selected",
                        message = "Select a folder to browse your project files.",
                        actionLabel = "Browse Files",
                        onAction = { folderPickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.isLoading && uiState.files.isEmpty() -> {
                    LoadingShimmerList(
                        count = 6,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                uiState.error != null && uiState.files.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Close,
                        title = "Error loading files",
                        message = uiState.error ?: "An unknown error occurred",
                        actionLabel = "Retry",
                        onAction = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.isSearching -> {
                    SearchResults(
                        results = uiState.searchResults,
                        query = uiState.searchQuery,
                        onClearSearch = {
                            viewModel.clearSearch()
                            showSearch = false
                        }
                    )
                }

                else -> {
                    FileList(
                        files = uiState.files,
                        isLoading = uiState.isLoading,
                        onFileClick = { file ->
                            if (file.isDirectory) {
                                viewModel.openFolder(
                                    Uri.parse(file.uri),
                                    file.name
                                )
                            }
                        },
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createFolder(name)
                showCreateFolderDialog = false
            }
        )
    }
}

@Composable
private fun BreadcrumbBar(
    breadcrumbs: List<Breadcrumb>,
    onBreadcrumbClick: (Int) -> Unit,
    onNavigateUp: () -> Unit,
    isGitRepository: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (breadcrumbs.size > 1) {
            IconButton(
                onClick = onNavigateUp,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Up",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        breadcrumbs.forEachIndexed { index, crumb ->
            val isLast = index == breadcrumbs.lastIndex
            Text(
                text = crumb.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isLast) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable(enabled = !isLast) { onBreadcrumbClick(index) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            if (!isLast) {
                Text(
                    text = "/",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        if (isGitRepository) {
            Spacer(modifier = Modifier.width(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Git",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search files...") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            }
        )
    }
}

@Composable
private fun FileList(
    files: List<WorkspaceFile>,
    isLoading: Boolean,
    onFileClick: (WorkspaceFile) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${files.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(files, key = { it.uri }) { file ->
            FileRow(
                file = file,
                onClick = { onFileClick(file) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun FileRow(
    file: WorkspaceFile,
    onClick: () -> Unit
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
            Icon(
                imageVector = fileIcon(file),
                contentDescription = null,
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!file.isDirectory && file.size > 0) {
                    Text(
                        text = formatFileSize(file.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (file.gitStatus != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = file.gitStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<WorkspaceFile>,
    query: String,
    onClearSearch: () -> Unit
) {
    if (results.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Search,
            title = "No results",
            message = "No files matching \"$query\"",
            actionLabel = "Clear search",
            onAction = onClearSearch,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                Text(
                    text = "${results.size} results for \"$query\"",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            items(results, key = { it.uri }) { file ->
                FileRow(
                    file = file,
                    onClick = {
                        if (file.isDirectory) {
                            // Search result folder navigation handled by ViewModel
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Folder") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onCreate(folderName) },
                enabled = folderName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun fileIcon(file: WorkspaceFile): ImageVector {
    if (file.isDirectory) return Icons.Filled.Folder
    return when {
        file.name.endsWith(".kt") || file.name.endsWith(".kts") || file.name.endsWith(".java") -> Icons.Filled.Code
        file.name.endsWith(".png") || file.name.endsWith(".jpg") || file.name.endsWith(".svg") -> Icons.Filled.Image
        file.name.endsWith(".md") || file.name.endsWith(".txt") || file.name.endsWith(".json") || file.name.endsWith(".yaml") || file.name.endsWith(".yml") -> Icons.Filled.Description
        else -> Icons.Filled.InsertDriveFile
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun WorkspaceScreenEmptyPreview() {
    RepoFlowTheme(darkTheme = true) {
        WorkspaceScreen()
    }
}
