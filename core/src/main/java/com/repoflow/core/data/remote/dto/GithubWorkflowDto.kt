package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubWorkflowDto(
    val id: Long,
    val name: String,
    val path: String,
    val state: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("badge_url") val badgeUrl: String
)

data class GithubWorkflowListResponse(
    @SerializedName("total_count") val totalCount: Int,
    val workflows: List<GithubWorkflowDto>
)
