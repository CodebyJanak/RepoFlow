package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubIssueDto(
    val id: Long,
    val number: Int,
    val title: String,
    val state: String,
    val body: String?,
    val user: GithubIssueUserDto,
    val labels: List<GithubLabelDto> = emptyList(),
    val assignees: List<GithubIssueUserDto> = emptyList(),
    val comments: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("html_url") val htmlUrl: String
)

data class GithubIssueUserDto(
    val login: String,
    val id: Long,
    @SerializedName("avatar_url") val avatarUrl: String,
    val htmlUrl: String? = null
)

data class GithubLabelDto(
    val id: Long,
    val name: String,
    val color: String,
    val description: String? = null
)
