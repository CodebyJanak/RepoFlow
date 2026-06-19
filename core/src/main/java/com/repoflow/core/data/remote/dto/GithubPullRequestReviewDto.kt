package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubPullRequestReviewDto(
    val id: Long,
    val user: GithubIssueUserDto,
    val body: String?,
    val state: String,
    @SerializedName("submitted_at") val submittedAt: String,
    @SerializedName("html_url") val htmlUrl: String? = null
)
