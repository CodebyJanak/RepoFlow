package com.repoflow.core.domain.model

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val message: String,
    val repositoryName: String,
    val timestamp: Long,
    val isSynced: Boolean = true
)

enum class ActivityType {
    COMMIT,
    PUSH,
    PULL,
    FETCH,
    CLONE,
    BRANCH_SWITCH,
    BRANCH_CREATE,
    BRANCH_DELETE,
    MERGE,
    ERROR
}
