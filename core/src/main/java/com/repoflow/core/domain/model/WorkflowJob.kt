package com.repoflow.core.domain.model

data class WorkflowJob(
    val id: Long,
    val runId: Long,
    val workflowName: String?,
    val headBranch: String?,
    val status: String?,
    val conclusion: String?,
    val name: String,
    val steps: List<JobStep> = emptyList(),
    val startedAt: String?,
    val completedAt: String?,
    val htmlUrl: String?
)

data class JobStep(
    val name: String,
    val status: String,
    val conclusion: String?,
    val number: Int,
    val startedAt: String?,
    val completedAt: String?
)
