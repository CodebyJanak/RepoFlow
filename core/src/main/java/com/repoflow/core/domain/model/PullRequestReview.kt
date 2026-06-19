package com.repoflow.core.domain.model

data class PullRequestReview(
    val id: Long,
    val user: IssueUser,
    val body: String?,
    val state: ReviewState,
    val submittedAt: String,
    val htmlUrl: String?
)

enum class ReviewState(val apiValue: String) {
    APPROVED("approved"),
    CHANGES_REQUESTED("changes_requested"),
    COMMENT("comment"),
    PENDING("pending");

    companion object {
        fun fromString(value: String): ReviewState = entries.find {
            it.apiValue == value.lowercase()
        } ?: COMMENT

        fun fromDisplay(value: String): ReviewState = when (value.lowercase()) {
            "approve" -> APPROVED
            "request_changes" -> CHANGES_REQUESTED
            "comment" -> COMMENT
            else -> COMMENT
        }
    }
}
