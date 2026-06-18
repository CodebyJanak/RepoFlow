package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubUserDto(
    val id: Long,
    val login: String,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    val name: String?,
    val email: String?,
    val bio: String?,
    @SerializedName("public_repos")
    val publicRepos: Int
)
