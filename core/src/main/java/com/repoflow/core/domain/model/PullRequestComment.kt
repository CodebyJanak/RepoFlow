package com.repoflow.core.domain.model

data class PullRequestComment(
    val id: Long,
    val body: String,
    val user: IssueUser,
    val path: String?,
    val position: Int?,
    val createdAt: String,
    val updatedAt: String,
    val htmlUrl: String
)
