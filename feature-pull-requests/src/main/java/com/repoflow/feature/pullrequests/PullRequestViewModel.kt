package com.repoflow.feature.pullrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.PullRequest
import com.repoflow.core.domain.model.PullRequestComment
import com.repoflow.core.domain.model.PullRequestReview
import com.repoflow.core.domain.model.PullRequestState
import com.repoflow.core.domain.repository.MergeResult
import com.repoflow.core.domain.repository.PullRequestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PullRequestsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pullRequests: List<PullRequest> = emptyList(),
    val currentState: PullRequestState = PullRequestState.OPEN
)

data class PullRequestDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pullRequest: PullRequest? = null,
    val reviews: List<PullRequestReview> = emptyList(),
    val reviewComments: List<PullRequestComment> = emptyList(),
    val isReviewsLoading: Boolean = false,
    val isCommentsLoading: Boolean = false,
    val mergeResult: MergeResult? = null,
    val mergeError: String? = null
)

data class CreatePullRequestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class PullRequestViewModel @Inject constructor(
    private val pullRequestsRepository: PullRequestsRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(PullRequestsUiState())
    val listState: StateFlow<PullRequestsUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(PullRequestDetailUiState())
    val detailState: StateFlow<PullRequestDetailUiState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow(CreatePullRequestUiState())
    val createState: StateFlow<CreatePullRequestUiState> = _createState.asStateFlow()

    fun loadPullRequests(owner: String, repo: String, state: PullRequestState = PullRequestState.OPEN) {
        _listState.value = _listState.value.copy(isLoading = true, error = null, currentState = state)
        viewModelScope.launch {
            val result = pullRequestsRepository.getPullRequests(owner, repo, state)
            result.onSuccess { prs ->
                _listState.value = _listState.value.copy(pullRequests = prs, isLoading = false)
            }
            result.onFailure { e ->
                _listState.value = _listState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun setFilter(owner: String, repo: String, state: PullRequestState) {
        loadPullRequests(owner, repo, state)
    }

    fun loadPullRequestDetail(owner: String, repo: String, pullNumber: Int) {
        _detailState.value = _detailState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = pullRequestsRepository.getPullRequest(owner, repo, pullNumber)
            result.onSuccess { pr ->
                _detailState.value = _detailState.value.copy(pullRequest = pr, isLoading = false)
                loadReviews(owner, repo, pullNumber)
                loadComments(owner, repo, pullNumber)
            }
            result.onFailure { e ->
                _detailState.value = _detailState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadReviews(owner: String, repo: String, pullNumber: Int) {
        _detailState.value = _detailState.value.copy(isReviewsLoading = true)
        viewModelScope.launch {
            val result = pullRequestsRepository.getPullRequestReviews(owner, repo, pullNumber)
            result.onSuccess { reviews ->
                _detailState.value = _detailState.value.copy(reviews = reviews, isReviewsLoading = false)
            }
            result.onFailure {
                _detailState.value = _detailState.value.copy(isReviewsLoading = false)
            }
        }
    }

    fun loadComments(owner: String, repo: String, pullNumber: Int) {
        _detailState.value = _detailState.value.copy(isCommentsLoading = true)
        viewModelScope.launch {
            val result = pullRequestsRepository.getPullRequestComments(owner, repo, pullNumber)
            result.onSuccess { comments ->
                _detailState.value = _detailState.value.copy(reviewComments = comments, isCommentsLoading = false)
            }
            result.onFailure {
                _detailState.value = _detailState.value.copy(isCommentsLoading = false)
            }
        }
    }

    fun submitReview(owner: String, repo: String, pullNumber: Int, body: String, event: String) {
        viewModelScope.launch {
            val result = pullRequestsRepository.createPullRequestReview(owner, repo, pullNumber, body, event)
            result.onSuccess {
                loadReviews(owner, repo, pullNumber)
            }
            result.onFailure { e ->
                _detailState.value = _detailState.value.copy(error = e.message)
            }
        }
    }

    fun mergePullRequest(owner: String, repo: String, pullNumber: Int, mergeMethod: String = "merge") {
        _detailState.value = _detailState.value.copy(mergeError = null)
        viewModelScope.launch {
            val result = pullRequestsRepository.mergePullRequest(owner, repo, pullNumber, mergeMethod)
            result.onSuccess { mergeResult ->
                _detailState.value = _detailState.value.copy(mergeResult = mergeResult)
                loadPullRequestDetail(owner, repo, pullNumber)
            }
            result.onFailure { e ->
                _detailState.value = _detailState.value.copy(mergeError = e.message)
            }
        }
    }

    fun addComment(owner: String, repo: String, pullNumber: Int, body: String) {
        viewModelScope.launch {
            val result = pullRequestsRepository.createPullRequestComment(owner, repo, pullNumber, body)
            result.onSuccess {
                loadComments(owner, repo, pullNumber)
            }
            result.onFailure { e ->
                _detailState.value = _detailState.value.copy(error = e.message)
            }
        }
    }

    fun createPullRequest(owner: String, repo: String, title: String, body: String?, head: String, base: String, draft: Boolean = false) {
        _createState.value = _createState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = pullRequestsRepository.createPullRequest(owner, repo, title, body, head, base, draft)
            result.onSuccess {
                _createState.value = _createState.value.copy(isLoading = false, isSuccess = true)
            }
            result.onFailure { e ->
                _createState.value = _createState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreatePullRequestUiState()
    }
}
