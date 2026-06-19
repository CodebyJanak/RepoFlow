package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubWorkflowRunDto(
    val id: Long,
    val name: String?,
    @SerializedName("run_number") val runNumber: Int,
    @SerializedName("run_attempt") val runAttempt: Int = 1,
    val status: String?,
    val conclusion: String?,
    @SerializedName("head_branch") val headBranch: String?,
    @SerializedName("head_sha") val headSha: String,
    @SerializedName("display_title") val displayTitle: String?,
    val actor: GithubIssueUserDto?,
    @SerializedName("triggering_actor") val triggeringActor: GithubIssueUserDto?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("run_started_at") val runStartedAt: String?,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("workflow_id") val workflowId: Long,
    @SerializedName("workflow_name") val workflowName: String?
)

data class GithubWorkflowRunListResponse(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("workflow_runs") val workflowRuns: List<GithubWorkflowRunDto>
)
