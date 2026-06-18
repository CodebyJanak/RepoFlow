package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubBranchDto(
    val name: String,
    val commit: GithubBranchCommitDto,
    @SerializedName("protected")
    val isProtected: Boolean
)

data class GithubBranchCommitDto(
    val sha: String,
    val url: String
)
