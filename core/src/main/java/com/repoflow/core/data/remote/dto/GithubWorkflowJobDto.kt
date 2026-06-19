package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubWorkflowJobDto(
    val id: Long,
    @SerializedName("run_id") val runId: Long,
    @SerializedName("workflow_name") val workflowName: String?,
    @SerializedName("head_branch") val headBranch: String?,
    val status: String?,
    val conclusion: String?,
    val name: String,
    val steps: List<GithubJobStepDto> = emptyList(),
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("html_url") val htmlUrl: String?
)

data class GithubJobStepDto(
    val name: String,
    val status: String,
    val conclusion: String?,
    val number: Int,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?
)

data class GithubWorkflowJobListResponse(
    @SerializedName("total_count") val totalCount: Int,
    val jobs: List<GithubWorkflowJobDto>
)
