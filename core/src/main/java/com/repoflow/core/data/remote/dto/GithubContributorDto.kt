package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubContributorDto(
    val login: String,
    val id: Long,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    val contributions: Int
)
