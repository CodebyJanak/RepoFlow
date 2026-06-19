package com.repoflow.feature.gitpilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.ChangelogResult
import com.repoflow.core.domain.model.CommitSuggestion
import com.repoflow.core.domain.model.ConflictAnalysis
import com.repoflow.core.domain.model.GitErrorAnalysis
import com.repoflow.core.domain.model.RepoSummary
import com.repoflow.core.domain.repository.GitPilotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommitMessageUiState(
    val diff: String = "",
    val isLoading: Boolean = false,
    val result: CommitSuggestion? = null,
    val error: String? = null
)

data class ChangelogUiState(
    val gitLog: String = "",
    val isLoading: Boolean = false,
    val result: ChangelogResult? = null,
    val error: String? = null
)

data class GitErrorUiState(
    val errorText: String = "",
    val isLoading: Boolean = false,
    val result: GitErrorAnalysis? = null,
    val error: String? = null
)

data class ConflictUiState(
    val conflictText: String = "",
    val isLoading: Boolean = false,
    val result: ConflictAnalysis? = null,
    val error: String? = null
)

data class RepoSummaryUiState(
    val repoName: String = "",
    val repoDescription: String = "",
    val primaryLanguage: String = "",
    val commitCount: Int = 0,
    val branchCount: Int = 0,
    val contributorCount: Int = 0,
    val recentCommits: String = "",
    val isLoading: Boolean = false,
    val result: RepoSummary? = null,
    val error: String? = null
)

@HiltViewModel
class GitPilotViewModel @Inject constructor(
    private val repository: GitPilotRepository
) : ViewModel() {

    private val _commitState = MutableStateFlow(CommitMessageUiState())
    val commitState: StateFlow<CommitMessageUiState> = _commitState.asStateFlow()

    private val _changelogState = MutableStateFlow(ChangelogUiState())
    val changelogState: StateFlow<ChangelogUiState> = _changelogState.asStateFlow()

    private val _errorState = MutableStateFlow(GitErrorUiState())
    val errorState: StateFlow<GitErrorUiState> = _errorState.asStateFlow()

    private val _conflictState = MutableStateFlow(ConflictUiState())
    val conflictState: StateFlow<ConflictUiState> = _conflictState.asStateFlow()

    private val _summaryState = MutableStateFlow(RepoSummaryUiState())
    val summaryState: StateFlow<RepoSummaryUiState> = _summaryState.asStateFlow()

    fun updateCommitDiff(diff: String) {
        _commitState.update { it.copy(diff = diff) }
    }

    fun generateCommitMessage() {
        val diff = _commitState.value.diff
        if (diff.isBlank()) return

        _commitState.update { it.copy(isLoading = true, error = null, result = null) }
        viewModelScope.launch {
            val result = repository.generateCommitMessage(diff)
            result.fold(
                onSuccess = { suggestion ->
                    _commitState.update { it.copy(isLoading = false, result = suggestion) }
                },
                onFailure = { e ->
                    _commitState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun updateChangelogLog(log: String) {
        _changelogState.update { it.copy(gitLog = log) }
    }

    fun generateChangelog() {
        val log = _changelogState.value.gitLog
        if (log.isBlank()) return

        _changelogState.update { it.copy(isLoading = true, error = null, result = null) }
        viewModelScope.launch {
            val result = repository.generateChangelog(log)
            result.fold(
                onSuccess = { changelog ->
                    _changelogState.update { it.copy(isLoading = false, result = changelog) }
                },
                onFailure = { e ->
                    _changelogState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun updateErrorText(text: String) {
        _errorState.update { it.copy(errorText = text) }
    }

    fun explainError() {
        val text = _errorState.value.errorText
        if (text.isBlank()) return

        _errorState.update { it.copy(isLoading = true, error = null, result = null) }
        viewModelScope.launch {
            val result = repository.explainGitError(text)
            result.fold(
                onSuccess = { analysis ->
                    _errorState.update { it.copy(isLoading = false, result = analysis) }
                },
                onFailure = { e ->
                    _errorState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun updateConflictText(text: String) {
        _conflictState.update { it.copy(conflictText = text) }
    }

    fun explainConflict() {
        val text = _conflictState.value.conflictText
        if (text.isBlank()) return

        _conflictState.update { it.copy(isLoading = true, error = null, result = null) }
        viewModelScope.launch {
            val result = repository.explainMergeConflict(text)
            result.fold(
                onSuccess = { analysis ->
                    _conflictState.update { it.copy(isLoading = false, result = analysis) }
                },
                onFailure = { e ->
                    _conflictState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun updateSummaryData(
        name: String, description: String, language: String,
        commits: Int, branches: Int, contributors: Int, recentLogs: String
    ) {
        _summaryState.update {
            it.copy(
                repoName = name, repoDescription = description,
                primaryLanguage = language, commitCount = commits,
                branchCount = branches, contributorCount = contributors,
                recentCommits = recentLogs
            )
        }
    }

    fun generateSummary() {
        val s = _summaryState.value
        _summaryState.update { it.copy(isLoading = true, error = null, result = null) }
        viewModelScope.launch {
            val result = repository.summarizeRepository(
                name = s.repoName,
                description = s.repoDescription.ifBlank { null },
                language = s.primaryLanguage.ifBlank { null },
                commitCount = s.commitCount,
                branchCount = s.branchCount,
                contributorCount = s.contributorCount,
                recentCommits = s.recentCommits
            )
            result.fold(
                onSuccess = { summary ->
                    _summaryState.update { it.copy(isLoading = false, result = summary) }
                },
                onFailure = { e ->
                    _summaryState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearErrors() {
        _commitState.update { it.copy(error = null) }
        _changelogState.update { it.copy(error = null) }
        _errorState.update { it.copy(error = null) }
        _conflictState.update { it.copy(error = null) }
        _summaryState.update { it.copy(error = null) }
    }
}
