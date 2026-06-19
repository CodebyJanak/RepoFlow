package com.repoflow.core.data.remote.dto

data class GithubPullRequestMergeDto(
    val sha: String,
    val merged: Boolean,
    val message: String
)
