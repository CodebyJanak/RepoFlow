package com.repoflow.core.domain.model

data class Issue(
    val id: Long,
    val number: Int,
    val title: String,
    val state: IssueState,
    val body: String?,
    val user: IssueUser,
    val labels: List<IssueLabel> = emptyList(),
    val assignees: List<IssueUser> = emptyList(),
    val comments: Int,
    val createdAt: String,
    val updatedAt: String,
    val closedAt: String?,
    val htmlUrl: String
)

enum class IssueState {
    OPEN, CLOSED, ALL;

    companion object {
        fun fromString(value: String): IssueState = when (value.lowercase()) {
            "open" -> OPEN
            "closed" -> CLOSED
            else -> ALL
        }
    }
}

data class IssueUser(
    val login: String,
    val id: Long,
    val avatarUrl: String
)

data class IssueLabel(
    val id: Long,
    val name: String,
    val color: String
)
