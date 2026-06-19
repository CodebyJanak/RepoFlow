package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubPullRequestDto(
    val id: Long,
    val number: Int,
    val state: String,
    val title: String,
    val body: String?,
    val user: GithubIssueUserDto,
    val head: GithubPullRequestBranchDto,
    val base: GithubPullRequestBranchDto,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("merged_at") val mergedAt: String?,
    val mergeable: Boolean?,
    val merged: Boolean,
    val comments: Int,
    @SerializedName("review_comments") val reviewComments: Int,
    val commits: Int,
    val additions: Int?,
    val deletions: Int?,
    @SerializedName("changed_files") val changedFiles: Int?,
    @SerializedName("html_url") val htmlUrl: String
)

data class GithubPullRequestBranchDto(
    val label: String,
    val ref: String,
    val sha: String,
    val repo: GithubPullRequestRepoDto?
)

data class GithubPullRequestRepoDto(
    val id: Long,
    val name: String,
    @SerializedName("full_name") val fullName: String
)
