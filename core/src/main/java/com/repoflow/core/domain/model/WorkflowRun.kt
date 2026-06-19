package com.repoflow.core.domain.model

data class WorkflowRun(
    val id: Long,
    val name: String?,
    val runNumber: Int,
    val runAttempt: Int,
    val status: RunStatus,
    val conclusion: RunConclusion?,
    val headBranch: String?,
    val headSha: String,
    val displayTitle: String?,
    val actor: IssueUser?,
    val triggeringActor: IssueUser?,
    val createdAt: String,
    val updatedAt: String,
    val runStartedAt: String?,
    val htmlUrl: String,
    val workflowId: Long,
    val workflowName: String?
)

enum class RunStatus(val apiValue: String) {
    QUEUED("queued"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    PENDING("pending"),
    REQUESTED("requested"),
    WAITING("waiting"),
    CANCELLED("cancelled"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(value: String?): RunStatus = entries.find {
            it.apiValue == value
        } ?: UNKNOWN
    }

    val isActive: Boolean get() = this == QUEUED || this == IN_PROGRESS || this == PENDING || this == REQUESTED || this == WAITING
}

enum class RunConclusion(val apiValue: String) {
    SUCCESS("success"),
    FAILURE("failure"),
    CANCELLED("cancelled"),
    SKIPPED("skipped"),
    TIMED_OUT("timed_out"),
    NEUTRAL("neutral"),
    ACTION_REQUIRED("action_required"),
    STALE("stale"),
    STARTUP_FAILURE("startup_failure"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(value: String?): RunConclusion? = entries.find {
            it.apiValue == value
        }
    }

    val isSuccess: Boolean get() = this == SUCCESS
    val isFailure: Boolean get() = this == FAILURE || this == TIMED_OUT || this == STARTUP_FAILURE
}
