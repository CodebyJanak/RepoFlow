package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubPullRequestCommentDto(
    val id: Long,
    val body: String,
    val user: GithubIssueUserDto,
    val path: String?,
    val position: Int?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("html_url") val htmlUrl: String
)
