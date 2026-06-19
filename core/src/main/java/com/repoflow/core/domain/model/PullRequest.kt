package com.repoflow.core.domain.model

data class PullRequest(
    val id: Long,
    val number: Int,
    val state: PullRequestState,
    val title: String,
    val body: String?,
    val user: IssueUser,
    val head: PullRequestBranch,
    val base: PullRequestBranch,
    val createdAt: String,
    val updatedAt: String,
    val closedAt: String?,
    val mergedAt: String?,
    val mergeable: Boolean?,
    val merged: Boolean,
    val comments: Int,
    val reviewComments: Int,
    val commits: Int,
    val additions: Int?,
    val deletions: Int?,
    val changedFiles: Int?,
    val htmlUrl: String
)

enum class PullRequestState {
    OPEN, CLOSED, MERGED, ALL;

    companion object {
        fun fromString(value: String): PullRequestState = when (value.lowercase()) {
            "open" -> OPEN
            "closed" -> CLOSED
            "merged" -> MERGED
            else -> ALL
        }
    }
}

data class PullRequestBranch(
    val label: String,
    val ref: String,
    val sha: String,
    val repoName: String?,
    val repoFullName: String?
)
