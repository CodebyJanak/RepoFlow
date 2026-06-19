package com.repoflow.core.data.mapper

import com.repoflow.core.data.remote.dto.GithubPullRequestCommentDto
import com.repoflow.core.data.remote.dto.GithubPullRequestDto
import com.repoflow.core.data.remote.dto.GithubPullRequestReviewDto
import com.repoflow.core.domain.model.IssueUser
import com.repoflow.core.domain.model.PullRequest
import com.repoflow.core.domain.model.PullRequestBranch
import com.repoflow.core.domain.model.PullRequestComment
import com.repoflow.core.domain.model.PullRequestReview
import com.repoflow.core.domain.model.PullRequestState
import com.repoflow.core.domain.model.ReviewState

object PullRequestMapper {

    fun GithubPullRequestDto.toDomain(): PullRequest = PullRequest(
        id = id,
        number = number,
        state = PullRequestState.fromString(
            if (merged) "merged" else state
        ),
        title = title,
        body = body,
        user = IssueUser(
            login = user.login,
            id = user.id,
            avatarUrl = user.avatarUrl
        ),
        head = PullRequestBranch(
            label = head.label,
            ref = head.ref,
            sha = head.sha,
            repoName = head.repo?.name,
            repoFullName = head.repo?.fullName
        ),
        base = PullRequestBranch(
            label = base.label,
            ref = base.ref,
            sha = base.sha,
            repoName = base.repo?.name,
            repoFullName = base.repo?.fullName
        ),
        createdAt = createdAt,
        updatedAt = updatedAt,
        closedAt = closedAt,
        mergedAt = mergedAt,
        mergeable = mergeable,
        merged = merged,
        comments = comments,
        reviewComments = reviewComments,
        commits = commits,
        additions = additions,
        deletions = deletions,
        changedFiles = changedFiles,
        htmlUrl = htmlUrl
    )

    fun GithubPullRequestReviewDto.toDomain(): PullRequestReview = PullRequestReview(
        id = id,
        user = IssueUser(
            login = user.login,
            id = user.id,
            avatarUrl = user.avatarUrl
        ),
        body = body,
        state = ReviewState.fromString(state),
        submittedAt = submittedAt,
        htmlUrl = htmlUrl
    )

    fun GithubPullRequestCommentDto.toDomain(): PullRequestComment = PullRequestComment(
        id = id,
        body = body,
        user = IssueUser(
            login = user.login,
            id = user.id,
            avatarUrl = user.avatarUrl
        ),
        path = path,
        position = position,
        createdAt = createdAt,
        updatedAt = updatedAt,
        htmlUrl = htmlUrl
    )
}
