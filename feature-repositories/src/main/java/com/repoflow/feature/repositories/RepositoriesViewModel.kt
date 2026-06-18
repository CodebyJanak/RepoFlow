package com.repoflow.feature.repositories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.data.local.dao.RepositoryDao
import com.repoflow.core.data.mapper.RepositoryMapper.toDomain
import com.repoflow.core.data.repository.SortOrder
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.domain.repository.GitRepository as GitRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepositoriesUiState(
    val repositories: List<GitRepository> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.UPDATED,
    val showFavoritesOnly: Boolean = false
)

@HiltViewModel
class RepositoriesViewModel @Inject constructor(
    private val gitRepository: GitRepositoryInterface,
    private val repositoryDao: RepositoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepositoriesUiState(isLoading = true))
    val uiState: StateFlow<RepositoriesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var localObservationJob: Job? = null

    init {
        loadRepositories()
    }

    private fun observeLocalRepositories() {
        localObservationJob?.cancel()
        localObservationJob = viewModelScope.launch {
            val state = _uiState.value
            val flow = when {
                state.searchQuery.isNotBlank() -> repositoryDao.searchRepositories(state.searchQuery)
                state.showFavoritesOnly -> {
                    when (state.sortOrder) {
                        SortOrder.STARS -> repositoryDao.getFavoriteRepositories().map { list ->
                            list.sortedByDescending { it.stars }
                        }
                        else -> repositoryDao.getFavoriteRepositories()
                    }
                }
                else -> when (state.sortOrder) {
                    SortOrder.STARS -> repositoryDao.getAllRepositoriesSortedByStars()
                    SortOrder.NAME -> repositoryDao.getAllRepositoriesSortedByName()
                    SortOrder.FORKS -> repositoryDao.getAllRepositoriesSortedByForks()
                    SortOrder.UPDATED -> repositoryDao.getAllRepositories()
                }
            }
            flow.collect { entities ->
                _uiState.value = _uiState.value.copy(
                    repositories = entities.map { it.toDomain() },
                    isLoading = false
                )
            }
        }
    }

    fun loadRepositories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            observeLocalRepositories()
            val result = gitRepository.getRepositories()
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = _uiState.value.repositories.isEmpty()
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val result = gitRepository.getRepositories()
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (query.isNotBlank()) {
                val result = gitRepository.searchRepositories(query)
                result.onSuccess { repos ->
                    _uiState.value = _uiState.value.copy(repositories = repos)
                }
            }
            observeLocalRepositories()
        }
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
        observeLocalRepositories()
    }

    fun toggleFavoritesFilter() {
        _uiState.value = _uiState.value.copy(
            showFavoritesOnly = !_uiState.value.showFavoritesOnly
        )
        observeLocalRepositories()
    }

    fun toggleFavorite(repoId: Long, current: Boolean) {
        viewModelScope.launch {
            gitRepository.toggleFavorite(repoId, !current)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
