package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubRepoDto(
    val id: Long,
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    val description: String?,
    @SerializedName("private")
    val isPrivate: Boolean,
    @SerializedName("stargazers_count")
    val stargazersCount: Int,
    @SerializedName("forks_count")
    val forksCount: Int,
    val language: String?,
    @SerializedName("open_issues_count")
    val openIssuesCount: Int,
    @SerializedName("default_branch")
    val defaultBranch: String,
    val owner: GithubRepoOwnerDto
)

data class GithubRepoOwnerDto(
    val id: Long,
    val login: String,
    @SerializedName("avatar_url")
    val avatarUrl: String
)
