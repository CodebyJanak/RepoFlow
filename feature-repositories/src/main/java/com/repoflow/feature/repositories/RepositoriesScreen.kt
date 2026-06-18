package com.repoflow.feature.repositories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.data.repository.SortOrder
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.LoadingShimmerList
import com.repoflow.core.ui.components.RepoCard
import com.repoflow.core.ui.components.RepoFlowFloatingActionButton
import com.repoflow.core.ui.components.RepoFlowTopAppBar
import com.repoflow.core.ui.components.SearchBar

@Composable
fun RepositoriesScreen(
    onNavigateToDetail: (String, String) -> Unit = { _, _ -> },
    viewModel: RepositoriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        RepoFlowTopAppBar(
            title = "Repositories",
            navigationIcon = null,
            onNavigationClick = null,
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.toggleFavoritesFilter() }) {
                    Icon(
                        imageVector = if (state.showFavoritesOnly) Icons.Filled.Star
                        else Icons.Outlined.Favorite,
                        contentDescription = "Favorites",
                        tint = if (state.showFavoritesOnly) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        when {
            state.isLoading && state.repositories.isEmpty() -> {
                LoadingShimmerList(
                    count = 5,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            state.repositories.isEmpty() -> {
                val isEmptySearch = state.searchQuery.isNotBlank()
                EmptyState(
                    icon = if (isEmptySearch) Icons.Filled.SearchOff else Icons.Filled.FolderOff,
                    title = if (isEmptySearch) "No results found"
                    else if (state.showFavoritesOnly) "No favorites yet"
                    else "No repositories",
                    message = if (isEmptySearch) "Try searching with a different query."
                    else if (state.showFavoritesOnly) "Star your favorite repos to see them here."
                    else "Your repositories will appear here.",
                    actionLabel = if (!isEmptySearch && !state.showFavoritesOnly) "Refresh" else null,
                    onAction = if (!isEmptySearch && !state.showFavoritesOnly) {{ viewModel.refresh() }} else null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            SearchBar(
                                query = state.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChanged,
                                onSearch = {},
                                placeholder = "Search repositories..."
                            )
                        }

                        item {
                            SortChipsRow(
                                currentSort = state.sortOrder,
                                onSortChanged = viewModel::onSortOrderChanged
                            )
                        }

                        items(
                            items = state.repositories,
                            key = { it.id }
                        ) { repo ->
                            RepoCard(
                                name = repo.fullName,
                                description = repo.description ?: "",
                                language = repo.language ?: "",
                                stars = repo.stars,
                                forks = repo.forks,
                                isPrivate = repo.isPrivate,
                                isFavorite = repo.isFavorite,
                                onClick = { onNavigateToDetail(repo.owner.login, repo.name) },
                                onFavoriteClick = { viewModel.toggleFavorite(repo.id, repo.isFavorite) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        RepoFlowFloatingActionButton(
            icon = Icons.Filled.Add,
            onClick = { /* TODO: New repository dialog */ }
        )
    }
}

@Composable
private fun SortChipsRow(
    currentSort: SortOrder,
    onSortChanged: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        SortOrder.entries.forEach { sort ->
            FilterChip(
                selected = currentSort == sort,
                onClick = { onSortChanged(sort) },
                label = {
                    Text(
                        text = when (sort) {
                            SortOrder.UPDATED -> "Updated"
                            SortOrder.STARS -> "Stars"
                            SortOrder.NAME -> "Name"
                            SortOrder.FORKS -> "Forks"
                        }
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
