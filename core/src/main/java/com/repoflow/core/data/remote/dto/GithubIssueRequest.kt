package com.repoflow.core.data.remote.dto

data class CreateIssueRequest(
    val title: String,
    val body: String? = null,
    val labels: List<String>? = null,
    val assignees: List<String>? = null
)

data class EditIssueRequest(
    val title: String? = null,
    val body: String? = null,
    val state: String? = null,
    val labels: List<String>? = null,
    val assignees: List<String>? = null
)

data class CreateCommentRequest(
    val body: String
)
