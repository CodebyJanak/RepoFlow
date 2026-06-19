package com.repoflow.core.data.remote.dto

data class CreatePullRequestRequest(
    val title: String,
    val body: String? = null,
    val head: String,
    val base: String,
    val draft: Boolean = false
)

data class UpdatePullRequestRequest(
    val title: String? = null,
    val body: String? = null,
    val state: String? = null
)

data class CreatePullRequestReviewRequest(
    val body: String,
    val event: String,
    val comments: List<ReviewComment>? = null
)

data class ReviewComment(
    val path: String,
    val position: Int? = null,
    val body: String
)

data class MergePullRequestRequest(
    val commit_title: String? = null,
    val commit_message: String? = null,
    val merge_method: String = "merge"
)

data class CreatePullRequestCommentRequest(
    val body: String,
    val path: String? = null,
    val position: Int? = null,
    val commit_id: String? = null
)
