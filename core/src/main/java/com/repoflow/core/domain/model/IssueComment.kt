package com.repoflow.core.domain.model

data class IssueComment(
    val id: Long,
    val body: String,
    val user: IssueUser,
    val createdAt: String,
    val updatedAt: String,
    val htmlUrl: String
)
