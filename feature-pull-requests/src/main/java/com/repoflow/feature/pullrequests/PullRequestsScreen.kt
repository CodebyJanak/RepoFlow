package com.repoflow.feature.pullrequests

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repoflow.core.domain.model.PullRequest
import com.repoflow.core.domain.model.PullRequestState
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.LoadingShimmerList
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRequestsScreen(
    owner: String,
    name: String,
    onBack: () -> Unit,
    onPullRequestClick: (Int) -> Unit,
    onCreatePullRequest: () -> Unit,
    viewModel: PullRequestViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(owner, name) {
        viewModel.loadPullRequests(owner, name)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RepoFlowTopAppBar(
                title = "Pull Requests",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onCreatePullRequest) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create PR",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterChipsRow(
                currentState = state.currentState,
                onStateSelected = { viewModel.setFilter(owner, name, it) }
            )

            when {
                state.isLoading -> {
                    LoadingShimmerList(
                        count = 5,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                state.error != null && state.pullRequests.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Cancel,
                        title = "Failed to load PRs",
                        message = state.error ?: "Unknown error",
                        actionLabel = "Retry",
                        onAction = { viewModel.loadPullRequests(owner, name) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.pullRequests.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.CallMerge,
                        title = "No pull requests",
                        message = "There are no ${state.currentState.name.lowercase()} pull requests.",
                        actionLabel = "New PR",
                        onAction = onCreatePullRequest,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }

                        items(state.pullRequests, key = { it.id }) { pr ->
                            PullRequestItem(
                                pullRequest = pr,
                                onClick = { onPullRequestClick(pr.number) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    currentState: PullRequestState,
    onStateSelected: (PullRequestState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PullRequestState.entries.filter { it != PullRequestState.ALL }.forEach { state ->
            FilterChip(
                selected = currentState == state,
                onClick = { onStateSelected(state) },
                label = { Text(state.name.lowercase().replaceFirstChar { it.uppercase() }) },
                icon = {
                    Icon(
                        imageVector = when (state) {
                            PullRequestState.MERGED -> Icons.Filled.CallMerge
                            PullRequestState.CLOSED -> Icons.Filled.CheckCircle
                            else -> if (currentState == state) Icons.Filled.RadioButtonChecked
                            else Icons.Filled.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun PullRequestItem(
    pullRequest: PullRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (pullRequest.state) {
                    PullRequestState.MERGED -> Icons.Filled.CallMerge
                    PullRequestState.CLOSED -> Icons.Filled.CheckCircle
                    else -> Icons.Filled.RadioButtonUnchecked
                },
                contentDescription = pullRequest.state.name,
                tint = when (pullRequest.state) {
                    PullRequestState.MERGED -> MaterialTheme.colorScheme.tertiary
                    PullRequestState.CLOSED -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pullRequest.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${pullRequest.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (pullRequest.user.avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = pullRequest.user.avatarUrl,
                            contentDescription = pullRequest.user.login,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = pullRequest.user.login,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${pullRequest.head.ref} -> ${pullRequest.base.ref}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (pullRequest.commits > 0) {
                        Text(
                            text = "${pullRequest.commits} commits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
