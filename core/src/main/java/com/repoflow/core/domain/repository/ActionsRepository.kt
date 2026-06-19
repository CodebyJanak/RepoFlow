package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.Artifact
import com.repoflow.core.domain.model.Workflow
import com.repoflow.core.domain.model.WorkflowJob
import com.repoflow.core.domain.model.WorkflowRun

interface ActionsRepository {

    suspend fun getWorkflows(
        owner: String,
        repo: String
    ): Result<List<Workflow>>

    suspend fun getWorkflowRuns(
        owner: String,
        repo: String,
        workflowId: Long,
        page: Int = 1
    ): Result<List<WorkflowRun>>

    suspend fun getRepositoryRuns(
        owner: String,
        repo: String,
        page: Int = 1
    ): Result<List<WorkflowRun>>

    suspend fun getWorkflowRun(
        owner: String,
        repo: String,
        runId: Long
    ): Result<WorkflowRun>

    suspend fun getWorkflowRunJobs(
        owner: String,
        repo: String,
        runId: Long
    ): Result<List<WorkflowJob>>

    suspend fun getRunArtifacts(
        owner: String,
        repo: String,
        runId: Long
    ): Result<List<Artifact>>

    suspend fun getArtifacts(
        owner: String,
        repo: String
    ): Result<List<Artifact>>
}
