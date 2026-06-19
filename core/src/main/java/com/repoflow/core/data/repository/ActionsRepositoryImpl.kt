package com.repoflow.core.data.repository

import com.repoflow.core.data.mapper.toDomain
import com.repoflow.core.data.remote.ApiService
import com.repoflow.core.domain.model.Artifact
import com.repoflow.core.domain.model.Workflow
import com.repoflow.core.domain.model.WorkflowJob
import com.repoflow.core.domain.model.WorkflowRun
import com.repoflow.core.domain.repository.ActionsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ActionsRepository {

    override suspend fun getWorkflows(owner: String, repo: String): Result<List<Workflow>> = runCatching {
        val response = apiService.getWorkflows(owner, repo)
        if (response.isSuccessful) {
            response.body()?.workflows?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load workflows: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getWorkflowRuns(
        owner: String,
        repo: String,
        workflowId: Long,
        page: Int
    ): Result<List<WorkflowRun>> = runCatching {
        val response = apiService.getWorkflowRuns(owner, repo, workflowId, page = page)
        if (response.isSuccessful) {
            response.body()?.workflowRuns?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load runs: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getRepositoryRuns(
        owner: String,
        repo: String,
        page: Int
    ): Result<List<WorkflowRun>> = runCatching {
        val response = apiService.getRepositoryRuns(owner, repo, page = page)
        if (response.isSuccessful) {
            response.body()?.workflowRuns?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load runs: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getWorkflowRun(
        owner: String,
        repo: String,
        runId: Long
    ): Result<WorkflowRun> = runCatching {
        val response = apiService.getWorkflowRun(owner, repo, runId)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Run not found")
        } else {
            throw Exception("Failed to load run: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getWorkflowRunJobs(
        owner: String,
        repo: String,
        runId: Long
    ): Result<List<WorkflowJob>> = runCatching {
        val response = apiService.getWorkflowRunJobs(owner, repo, runId)
        if (response.isSuccessful) {
            response.body()?.jobs?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load jobs: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getRunArtifacts(
        owner: String,
        repo: String,
        runId: Long
    ): Result<List<Artifact>> = runCatching {
        val response = apiService.getRunArtifacts(owner, repo, runId)
        if (response.isSuccessful) {
            response.body()?.artifacts?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load artifacts: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getArtifacts(
        owner: String,
        repo: String
    ): Result<List<Artifact>> = runCatching {
        val response = apiService.getArtifacts(owner, repo)
        if (response.isSuccessful) {
            response.body()?.artifacts?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to load artifacts: ${response.code()} ${response.message()}")
        }
    }
}
