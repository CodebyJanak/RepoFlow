package com.repoflow.core.data.repository

import com.repoflow.core.data.mapper.toDomain
import com.repoflow.core.data.remote.ApiService
import com.repoflow.core.data.remote.dto.CreateCommentRequest
import com.repoflow.core.data.remote.dto.CreateIssueRequest
import com.repoflow.core.data.remote.dto.EditIssueRequest
import com.repoflow.core.domain.model.Issue
import com.repoflow.core.domain.model.IssueComment
import com.repoflow.core.domain.model.IssueState
import com.repoflow.core.domain.repository.IssuesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : IssuesRepository {

    override suspend fun getIssues(
        owner: String,
        repo: String,
        state: IssueState,
        page: Int
    ): Result<List<Issue>> = runCatching {
        val stateParam = when (state) {
            IssueState.ALL -> "all"
            else -> state.name.lowercase()
        }
        val response = apiService.getIssues(
            owner = owner,
            repo = repo,
            state = stateParam,
            page = page
        )
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load issues: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getIssue(
        owner: String,
        repo: String,
        issueNumber: Int
    ): Result<Issue> = runCatching {
        val response = apiService.getIssue(owner, repo, issueNumber)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Issue not found")
        } else {
            throw Exception("Failed to load issue: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun createIssue(
        owner: String,
        repo: String,
        title: String,
        body: String?,
        labels: List<String>?,
        assignees: List<String>?
    ): Result<Issue> = runCatching {
        val request = CreateIssueRequest(
            title = title,
            body = body,
            labels = labels,
            assignees = assignees
        )
        val response = apiService.createIssue(owner, repo, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to create issue")
        } else {
            throw Exception("Failed to create issue: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun editIssue(
        owner: String,
        repo: String,
        issueNumber: Int,
        title: String?,
        body: String?,
        state: String?,
        labels: List<String>?,
        assignees: List<String>?
    ): Result<Issue> = runCatching {
        val request = EditIssueRequest(
            title = title,
            body = body,
            state = state,
            labels = labels,
            assignees = assignees
        )
        val response = apiService.editIssue(owner, repo, issueNumber, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to edit issue")
        } else {
            throw Exception("Failed to edit issue: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun closeIssue(
        owner: String,
        repo: String,
        issueNumber: Int
    ): Result<Issue> = runCatching {
        val request = EditIssueRequest(state = "closed")
        val response = apiService.editIssue(owner, repo, issueNumber, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to close issue")
        } else {
            throw Exception("Failed to close issue: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getIssueComments(
        owner: String,
        repo: String,
        issueNumber: Int
    ): Result<List<IssueComment>> = runCatching {
        val response = apiService.getIssueComments(owner, repo, issueNumber)
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load comments: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun createIssueComment(
        owner: String,
        repo: String,
        issueNumber: Int,
        body: String
    ): Result<IssueComment> = runCatching {
        val request = CreateCommentRequest(body = body)
        val response = apiService.createIssueComment(owner, repo, issueNumber, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Failed to create comment")
        } else {
            throw Exception("Failed to create comment: ${response.code()} ${response.message()}")
        }
    }
}
