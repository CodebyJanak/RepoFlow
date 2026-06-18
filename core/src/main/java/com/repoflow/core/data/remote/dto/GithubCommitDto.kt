package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubCommitDto(
    val sha: String,
    val commit: GithubCommitDetailDto,
    val author: GithubCommitAuthorDto?
)

data class GithubCommitDetailDto(
    val message: String,
    val author: GithubCommitAuthorDetailDto
)

data class GithubCommitAuthorDetailDto(
    val name: String,
    val email: String,
    val date: String
)

data class GithubCommitAuthorDto(
    val login: String,
    @SerializedName("avatar_url")
    val avatarUrl: String
)
