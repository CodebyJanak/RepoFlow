package com.repoflow.feature.issues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.Issue
import com.repoflow.core.domain.model.IssueComment
import com.repoflow.core.domain.model.IssueState
import com.repoflow.core.domain.repository.IssuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IssuesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val issues: List<Issue> = emptyList(),
    val currentState: IssueState = IssueState.OPEN
)

data class IssueDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val issue: Issue? = null,
    val comments: List<IssueComment> = emptyList(),
    val isCommentsLoading: Boolean = false,
    val commentError: String? = null
)

data class CreateIssueUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class IssueViewModel @Inject constructor(
    private val issuesRepository: IssuesRepository
) : ViewModel() {

    private val _issuesState = MutableStateFlow(IssuesUiState())
    val issuesState: StateFlow<IssuesUiState> = _issuesState.asStateFlow()

    private val _detailState = MutableStateFlow(IssueDetailUiState())
    val detailState: StateFlow<IssueDetailUiState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow(CreateIssueUiState())
    val createState: StateFlow<CreateIssueUiState> = _createState.asStateFlow()

    fun loadIssues(owner: String, repo: String, state: IssueState = IssueState.OPEN) {
        _issuesState.value = _issuesState.value.copy(isLoading = true, error = null, currentState = state)
        viewModelScope.launch {
            val result = issuesRepository.getIssues(owner, repo, state)
            result.onSuccess { issues ->
                _issuesState.value = _issuesState.value.copy(
                    issues = issues,
                    isLoading = false
                )
            }
            result.onFailure { e ->
                _issuesState.value = _issuesState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun setIssueFilter(owner: String, repo: String, state: IssueState) {
        loadIssues(owner, repo, state)
    }

    fun loadIssueDetail(owner: String, repo: String, issueNumber: Int) {
        _detailState.value = _detailState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = issuesRepository.getIssue(owner, repo, issueNumber)
            result.onSuccess { issue ->
                _detailState.value = _detailState.value.copy(
                    issue = issue,
                    isLoading = false
                )
                loadComments(owner, repo, issueNumber)
            }
            result.onFailure { e ->
                _detailState.value = _detailState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun loadComments(owner: String, repo: String, issueNumber: Int) {
        _detailState.value = _detailState.value.copy(isCommentsLoading = true, commentError = null)
        viewModelScope.launch {
            val result = issuesRepository.getIssueComments(owner, repo, issueNumber)
            result.onSuccess { comments ->
                _detailState.value = _detailState.value.copy(
                    comments = comments,
                    isCommentsLoading = false
                )
            }
            result.onFailure { e ->
                _detailState.value = _detailState.value.copy(
                    commentError = e.message,
                    isCommentsLoading = false
                )
            }
        }
    }

    fun createIssue(owner: String, repo: String, title: String, body: String?) {
        _createState.value = _createState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = issuesRepository.createIssue(owner, repo, title, body)
            result.onSuccess {
                _createState.value = _createState.value.copy(isLoading = false, isSuccess = true)
            }
            result.onFailure { e ->
                _createState.value = _createState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun editIssue(owner: String, repo: String, issueNumber: Int, title: String?, body: String?) {
        _createState.value = _createState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = issuesRepository.editIssue(owner, repo, issueNumber, title = title, body = body)
            result.onSuccess {
                _createState.value = _createState.value.copy(isLoading = false, isSuccess = true)
            }
            result.onFailure { e ->
                _createState.value = _createState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun closeIssue(owner: String, repo: String, issueNumber: Int) {
        viewModelScope.launch {
            val result = issuesRepository.closeIssue(owner, repo, issueNumber)
            result.onSuccess { issue ->
                _detailState.value = _detailState.value.copy(issue = issue)
                loadIssues(owner, repo, _issuesState.value.currentState)
            }
        }
    }

    fun addComment(owner: String, repo: String, issueNumber: Int, body: String) {
        _detailState.value = _detailState.value.copy(commentError = null)
        viewModelScope.launch {
            val result = issuesRepository.createIssueComment(owner, repo, issueNumber, body)
            result.onSuccess {
                loadComments(owner, repo, issueNumber)
            }
            result.onFailure { e ->
                _detailState.value = _detailState.value.copy(commentError = e.message)
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateIssueUiState()
    }
}
