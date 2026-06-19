package com.repoflow.feature.commit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.repository.GitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommitTemplate(
    val title: String,
    val prefix: String,
    val description: String
)

val commitTemplates = listOf(
    CommitTemplate("feat", "feat", "New feature"),
    CommitTemplate("fix", "fix", "Bug fix"),
    CommitTemplate("docs", "docs", "Documentation"),
    CommitTemplate("style", "style", "Code style"),
    CommitTemplate("refactor", "refactor", "Code refactoring"),
    CommitTemplate("perf", "perf", "Performance"),
    CommitTemplate("test", "test", "Tests"),
    CommitTemplate("chore", "chore", "Chores"),
)

data class CommitUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val commitResult: Commit? = null,
    val subject: String = "",
    val body: String = "",
    val authorName: String = "Developer",
    val authorEmail: String = "developer@repoflow.app",
    val stagedCount: Int = 0,
    val recentCommits: List<Commit> = emptyList(),
    val showHistorySheet: Boolean = false,
    val isCommitting: Boolean = false,
    val isHistoryLoading: Boolean = false,
    val validationErrors: List<String> = emptyList()
)

@HiltViewModel
class CommitViewModel @Inject constructor(
    private val gitRepository: GitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommitUiState())
    val uiState: StateFlow<CommitUiState> = _uiState.asStateFlow()

    private var localPath: String = ""

    fun loadCommitInfo(localPath: String) {
        this.localPath = localPath
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val statusResult = gitRepository.getStatus(localPath)
            val staged = statusResult.getOrNull()?.count { it.staged } ?: 0
            _uiState.value = _uiState.value.copy(stagedCount = staged, isLoading = false)
        }
    }

    fun setSubject(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject)
        validate()
    }

    fun setBody(body: String) {
        _uiState.value = _uiState.value.copy(body = body)
    }

    fun setAuthorName(name: String) {
        _uiState.value = _uiState.value.copy(authorName = name)
    }

    fun setAuthorEmail(email: String) {
        _uiState.value = _uiState.value.copy(authorEmail = email)
    }

    fun selectTemplate(template: CommitTemplate) {
        val current = _uiState.value.subject
        val newSubject = if (current.startsWith("${template.prefix}:")) {
            current
        } else {
            "${template.prefix}: ${current.replaceFirst(Regex("^\\w+:\\s*"), "")}"
        }
        _uiState.value = _uiState.value.copy(subject = newSubject)
    }

    fun commit() {
        val state = _uiState.value
        if (state.subject.isBlank()) return

        _uiState.value = state.copy(isCommitting = true, error = null)
        val fullMessage = if (state.body.isBlank()) state.subject else "${state.subject}\n\n${state.body}"

        viewModelScope.launch {
            val result = gitRepository.stageAll(localPath)
            result.onSuccess {
                val commitResult = gitRepository.commit(
                    localPath = localPath,
                    message = fullMessage,
                    authorName = state.authorName,
                    authorEmail = state.authorEmail
                )
                commitResult.onSuccess { commit ->
                    _uiState.value = _uiState.value.copy(
                        isCommitting = false,
                        isSuccess = true,
                        commitResult = commit
                    )
                }
                commitResult.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isCommitting = false,
                        error = e.message ?: "Commit failed"
                    )
                }
            }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isCommitting = false,
                    error = e.message ?: "Stage failed"
                )
            }
        }
    }

    fun commitAndPush() {
        val state = _uiState.value
        if (state.subject.isBlank()) return

        _uiState.value = state.copy(isCommitting = true, error = null)
        val fullMessage = if (state.body.isBlank()) state.subject else "${state.subject}\n\n${state.body}"

        viewModelScope.launch {
            val stage = gitRepository.stageAll(localPath)
            stage.onSuccess {
                val commitResult = gitRepository.commit(localPath, fullMessage, state.authorName, state.authorEmail)
                commitResult.onSuccess { commit ->
                    val pushResult = gitRepository.pushRepository(localPath)
                    pushResult.onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isCommitting = false,
                            isSuccess = true,
                            commitResult = commit
                        )
                    }
                    pushResult.onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isCommitting = false,
                            error = "Committed but push failed: ${e.message}",
                            isSuccess = true,
                            commitResult = commit
                        )
                    }
                }
                commitResult.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isCommitting = false,
                        error = e.message ?: "Commit failed"
                    )
                }
            }
            stage.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isCommitting = false,
                    error = e.message ?: "Stage failed"
                )
            }
        }
    }

    fun loadRecentCommits() {
        _uiState.value = _uiState.value.copy(isHistoryLoading = true)
        viewModelScope.launch {
            val result = gitRepository.getCommitHistory(localPath, maxCount = 20)
            result.onSuccess { commits ->
                _uiState.value = _uiState.value.copy(
                    recentCommits = commits,
                    isHistoryLoading = false,
                    showHistorySheet = true
                )
            }
            result.onFailure {
                _uiState.value = _uiState.value.copy(isHistoryLoading = false, showHistorySheet = true)
            }
        }
    }

    fun dismissHistory() {
        _uiState.value = _uiState.value.copy(showHistorySheet = false)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false, commitResult = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun validate() {
        val errors = mutableListOf<String>()
        val subject = _uiState.value.subject
        if (subject.length > 72) {
            errors.add("Subject exceeds 72 characters")
        } else if (subject.length > 50) {
            errors.add("Subject exceeds 50 character recommendation")
        }
        _uiState.value = _uiState.value.copy(validationErrors = errors)
    }
}
