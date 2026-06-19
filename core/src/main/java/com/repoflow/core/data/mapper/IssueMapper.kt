package com.repoflow.core.data.mapper

import com.repoflow.core.data.remote.dto.GithubIssueCommentDto
import com.repoflow.core.data.remote.dto.GithubIssueDto
import com.repoflow.core.domain.model.Issue
import com.repoflow.core.domain.model.IssueComment
import com.repoflow.core.domain.model.IssueLabel
import com.repoflow.core.domain.model.IssueState
import com.repoflow.core.domain.model.IssueUser

fun GithubIssueDto.toDomain(): Issue = Issue(
    id = id,
    number = number,
    title = title,
    state = IssueState.fromString(state),
    body = body,
    user = IssueUser(
        login = user.login,
        id = user.id,
        avatarUrl = user.avatarUrl
    ),
    labels = labels.map { label ->
        IssueLabel(
            id = label.id,
            name = label.name,
            color = label.color
        )
    },
    assignees = assignees.map { assignee ->
        IssueUser(
            login = assignee.login,
            id = assignee.id,
            avatarUrl = assignee.avatarUrl
        )
    },
    comments = comments,
    createdAt = createdAt,
    updatedAt = updatedAt,
    closedAt = closedAt,
    htmlUrl = htmlUrl
)

fun GithubIssueCommentDto.toDomain(): IssueComment = IssueComment(
    id = id,
    body = body,
    user = IssueUser(
        login = user.login,
        id = user.id,
        avatarUrl = user.avatarUrl
    ),
    createdAt = createdAt,
    updatedAt = updatedAt,
    htmlUrl = htmlUrl
)
