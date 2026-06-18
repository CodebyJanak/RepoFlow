package com.repoflow.feature.repositorydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.Branch
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.model.Contributor
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.domain.model.Release
import com.repoflow.core.domain.repository.GitRepository as GitRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepositoryDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val repository: GitRepository? = null,
    val branches: List<Branch> = emptyList(),
    val selectedBranch: Branch? = null,
    val commits: List<Commit> = emptyList(),
    val contributors: List<Contributor> = emptyList(),
    val releases: List<Release> = emptyList(),
    val isBranchesLoading: Boolean = false,
    val isCommitsLoading: Boolean = false,
    val isContributorsLoading: Boolean = false,
    val isReleasesLoading: Boolean = false,
    val cloneUrl: String = ""
)

@HiltViewModel
class RepositoryDetailViewModel @Inject constructor(
    private val gitRepository: GitRepositoryInterface
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepositoryDetailUiState())
    val uiState: StateFlow<RepositoryDetailUiState> = _uiState.asStateFlow()

    private var owner: String = ""
    private var name: String = ""

    fun loadRepository(owner: String, name: String) {
        this.owner = owner
        this.name = name
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val repoResult = gitRepository.getRepository(owner, name)
            repoResult.onSuccess { repo ->
                _uiState.value = _uiState.value.copy(
                    repository = repo,
                    cloneUrl = "https://github.com/$owner/$name.git",
                    isLoading = false
                )
                loadBranches()
                loadContributors()
                loadReleases()
            }
            repoResult.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun selectBranch(branch: Branch) {
        _uiState.value = _uiState.value.copy(selectedBranch = branch)
        loadCommits(branchName = branch.name)
    }

    private fun loadBranches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBranchesLoading = true)
            val result = gitRepository.getRemoteBranches(owner, name)
            result.onSuccess { branches ->
                val selected = _uiState.value.selectedBranch ?: branches.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    branches = branches,
                    selectedBranch = selected,
                    isBranchesLoading = false
                )
                if (selected != null && _uiState.value.commits.isEmpty()) {
                    loadCommits(selected.name)
                }
            }
            result.onFailure {
                _uiState.value = _uiState.value.copy(isBranchesLoading = false)
            }
        }
    }

    private fun loadCommits(branchName: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCommitsLoading = true)
            val branch = branchName ?: _uiState.value.selectedBranch?.name
            val result = gitRepository.getRemoteCommits(owner, name, branch)
            result.onSuccess { commits ->
                _uiState.value = _uiState.value.copy(
                    commits = commits,
                    isCommitsLoading = false
                )
            }
            result.onFailure {
                _uiState.value = _uiState.value.copy(isCommitsLoading = false)
            }
        }
    }

    private fun loadContributors() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isContributorsLoading = true)
            val result = gitRepository.getContributors(owner, name)
            result.onSuccess { contributors ->
                _uiState.value = _uiState.value.copy(
                    contributors = contributors,
                    isContributorsLoading = false
                )
            }
            result.onFailure {
                _uiState.value = _uiState.value.copy(isContributorsLoading = false)
            }
        }
    }

    private fun loadReleases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReleasesLoading = true)
            val result = gitRepository.getReleases(owner, name)
            result.onSuccess { releases ->
                _uiState.value = _uiState.value.copy(
                    releases = releases,
                    isReleasesLoading = false
                )
            }
            result.onFailure {
                _uiState.value = _uiState.value.copy(isReleasesLoading = false)
            }
        }
    }

    fun retry() {
        loadRepository(owner, name)
    }
}
