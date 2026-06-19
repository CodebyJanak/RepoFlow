package com.repoflow.core.data.repository

import com.repoflow.core.data.mapper.toDomain
import com.repoflow.core.data.remote.ApiService
import com.repoflow.core.data.remote.dto.CreatePullRequestRequest
import com.repoflow.core.data.remote.dto.CreatePullRequestCommentRequest
import com.repoflow.core.data.remote.dto.CreatePullRequestReviewRequest
import com.repoflow.core.data.remote.dto.MergePullRequestRequest
import com.repoflow.core.data.remote.dto.UpdatePullRequestRequest
import com.repoflow.core.domain.model.PullRequest
import com.repoflow.core.domain.model.PullRequestComment
import com.repoflow.core.domain.model.PullRequestReview
import com.repoflow.core.domain.model.PullRequestState
import com.repoflow.core.domain.repository.MergeResult
import com.repoflow.core.domain.repository.PullRequestsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PullRequestsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PullRequestsRepository {

    override suspend fun getPullRequests(
        owner: String,
        repo: String,
        state: PullRequestState,
        page: Int
    ): Result<List<PullRequest>> = runCatching {
        val stateParam = when (state) {
            PullRequestState.MERGED -> "closed"
            PullRequestState.ALL -> "all"
            else -> state.name.lowercase()
        }
        val response = apiService.getPullRequests(owner, repo, stateParam, page = page)
        if (response.isSuccessful) {
            val all = response.body()?.map { it.toDomain() } ?: emptyList()
            if (state == PullRequestState.MERGED) all.filter { it.merged } else all
        } else {
            throw Exception("Failed to load PRs: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getPullRequest(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<PullRequest> = runCatching {
        val response = apiService.getPullRequest(owner, repo, pullNumber)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Pull request not found")
        } else {
            throw Exception("Failed to load PR: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun createPullRequest(
        owner: String,
        repo: String,
        title: String,
        body: String?,
        head: String,
        base: String,
        draft: Boolean
    ): Result<PullRequest> = runCatching {
        val request = CreatePullRequestRequest(
            title = title,
            body = body,
            head = head,
            base = base,
            draft = draft
        )
        val response = apiService.createPullRequest(owner, repo, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to create PR")
        } else {
            throw Exception("Failed to create PR: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun updatePullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
        title: String?,
        body: String?,
        state: String?
    ): Result<PullRequest> = runCatching {
        val request = UpdatePullRequestRequest(title = title, body = body, state = state)
        val response = apiService.updatePullRequest(owner, repo, pullNumber, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to update PR")
        } else {
            throw Exception("Failed to update PR: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun closePullRequest(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<PullRequest> = updatePullRequest(owner, repo, pullNumber, state = "closed")

    override suspend fun getPullRequestReviews(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<List<PullRequestReview>> = runCatching {
        val response = apiService.getPullRequestReviews(owner, repo, pullNumber)
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load reviews: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun createPullRequestReview(
        owner: String,
        repo: String,
        pullNumber: Int,
        body: String,
        event: String
    ): Result<PullRequestReview> = runCatching {
        val request = CreatePullRequestReviewRequest(body = body, event = event)
        val response = apiService.createPullRequestReview(owner, repo, pullNumber, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to submit review")
        } else {
            throw Exception("Failed to submit review: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getPullRequestComments(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<List<PullRequestComment>> = runCatching {
        val response = apiService.getPullRequestComments(owner, repo, pullNumber)
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load comments: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun createPullRequestComment(
        owner: String,
        repo: String,
        pullNumber: Int,
        body: String,
        path: String?,
        position: Int?
    ): Result<PullRequestComment> = runCatching {
        val request = CreatePullRequestCommentRequest(
            body = body,
            path = path,
            position = position
        )
        val response = apiService.createPullRequestComment(owner, repo, pullNumber, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to create comment")
        } else {
            throw Exception("Failed to create comment: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun mergePullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
        mergeMethod: String
    ): Result<MergeResult> = runCatching {
        val request = MergePullRequestRequest(merge_method = mergeMethod)
        val response = apiService.mergePullRequest(owner, repo, pullNumber, request)
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Failed to merge PR")
            MergeResult(sha = body.sha, merged = body.merged, message = body.message)
        } else {
            throw Exception("Failed to merge PR: ${response.code()} ${response.message()}")
        }
    }
}
