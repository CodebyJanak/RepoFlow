package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.Issue
import com.repoflow.core.domain.model.IssueComment
import com.repoflow.core.domain.model.IssueState

interface IssuesRepository {

    suspend fun getIssues(
        owner: String,
        repo: String,
        state: IssueState = IssueState.OPEN,
        page: Int = 1
    ): Result<List<Issue>>

    suspend fun getIssue(
        owner: String,
        repo: String,
        issueNumber: Int
    ): Result<Issue>

    suspend fun createIssue(
        owner: String,
        repo: String,
        title: String,
        body: String?,
        labels: List<String>? = null,
        assignees: List<String>? = null
    ): Result<Issue>

    suspend fun editIssue(
        owner: String,
        repo: String,
        issueNumber: Int,
        title: String? = null,
        body: String? = null,
        state: String? = null,
        labels: List<String>? = null,
        assignees: List<String>? = null
    ): Result<Issue>

    suspend fun closeIssue(
        owner: String,
        repo: String,
        issueNumber: Int
    ): Result<Issue>

    suspend fun getIssueComments(
        owner: String,
        repo: String,
        issueNumber: Int
    ): Result<List<IssueComment>>

    suspend fun createIssueComment(
        owner: String,
        repo: String,
        issueNumber: Int,
        body: String
    ): Result<IssueComment>
}
