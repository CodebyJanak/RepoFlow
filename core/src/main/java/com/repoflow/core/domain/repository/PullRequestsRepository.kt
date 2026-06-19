package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.PullRequest
import com.repoflow.core.domain.model.PullRequestComment
import com.repoflow.core.domain.model.PullRequestReview
import com.repoflow.core.domain.model.PullRequestState
import com.repoflow.core.domain.model.ReviewState

interface PullRequestsRepository {

    suspend fun getPullRequests(
        owner: String,
        repo: String,
        state: PullRequestState = PullRequestState.OPEN,
        page: Int = 1
    ): Result<List<PullRequest>>

    suspend fun getPullRequest(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<PullRequest>

    suspend fun createPullRequest(
        owner: String,
        repo: String,
        title: String,
        body: String?,
        head: String,
        base: String,
        draft: Boolean = false
    ): Result<PullRequest>

    suspend fun updatePullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
        title: String? = null,
        body: String? = null,
        state: String? = null
    ): Result<PullRequest>

    suspend fun closePullRequest(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<PullRequest>

    suspend fun getPullRequestReviews(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<List<PullRequestReview>>

    suspend fun createPullRequestReview(
        owner: String,
        repo: String,
        pullNumber: Int,
        body: String,
        event: String
    ): Result<PullRequestReview>

    suspend fun getPullRequestComments(
        owner: String,
        repo: String,
        pullNumber: Int
    ): Result<List<PullRequestComment>>

    suspend fun createPullRequestComment(
        owner: String,
        repo: String,
        pullNumber: Int,
        body: String,
        path: String? = null,
        position: Int? = null
    ): Result<PullRequestComment>

    suspend fun mergePullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
        mergeMethod: String = "merge"
    ): Result<MergeResult>
}

data class MergeResult(
    val sha: String,
    val merged: Boolean,
    val message: String
)
