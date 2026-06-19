package com.repoflow.core.data.mapper

import com.repoflow.core.data.remote.dto.GithubArtifactDto
import com.repoflow.core.data.remote.dto.GithubJobStepDto
import com.repoflow.core.data.remote.dto.GithubWorkflowDto
import com.repoflow.core.data.remote.dto.GithubWorkflowJobDto
import com.repoflow.core.data.remote.dto.GithubWorkflowRunDto
import com.repoflow.core.domain.model.Artifact
import com.repoflow.core.domain.model.IssueUser
import com.repoflow.core.domain.model.JobStep
import com.repoflow.core.domain.model.RunConclusion
import com.repoflow.core.domain.model.RunStatus
import com.repoflow.core.domain.model.Workflow
import com.repoflow.core.domain.model.WorkflowJob
import com.repoflow.core.domain.model.WorkflowRun

object ActionsMapper {

    fun GithubWorkflowDto.toDomain(): Workflow = Workflow(
        id = id,
        name = name,
        path = path,
        state = state,
        createdAt = createdAt,
        updatedAt = updatedAt,
        htmlUrl = htmlUrl,
        badgeUrl = badgeUrl
    )

    fun GithubWorkflowRunDto.toDomain(): WorkflowRun = WorkflowRun(
        id = id,
        name = name,
        runNumber = runNumber,
        runAttempt = runAttempt,
        status = RunStatus.fromString(status),
        conclusion = RunConclusion.fromString(conclusion),
        headBranch = headBranch,
        headSha = headSha,
        displayTitle = displayTitle,
        actor = actor?.let { IssueUser(login = it.login, id = it.id, avatarUrl = it.avatarUrl) },
        triggeringActor = triggeringActor?.let { IssueUser(login = it.login, id = it.id, avatarUrl = it.avatarUrl) },
        createdAt = createdAt,
        updatedAt = updatedAt,
        runStartedAt = runStartedAt,
        htmlUrl = htmlUrl,
        workflowId = workflowId,
        workflowName = workflowName
    )

    fun GithubWorkflowJobDto.toDomain(): WorkflowJob = WorkflowJob(
        id = id,
        runId = runId,
        workflowName = workflowName,
        headBranch = headBranch,
        status = status,
        conclusion = conclusion,
        name = name,
        steps = steps.map { it.toDomain() },
        startedAt = startedAt,
        completedAt = completedAt,
        htmlUrl = htmlUrl
    )

    fun GithubJobStepDto.toDomain(): JobStep = JobStep(
        name = name,
        status = status,
        conclusion = conclusion,
        number = number,
        startedAt = startedAt,
        completedAt = completedAt
    )

    fun GithubArtifactDto.toDomain(): Artifact = Artifact(
        id = id,
        name = name,
        sizeInBytes = sizeInBytes,
        archiveDownloadUrl = archiveDownloadUrl,
        expired = expired,
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}
