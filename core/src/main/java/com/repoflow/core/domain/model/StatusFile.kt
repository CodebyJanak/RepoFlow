package com.repoflow.core.domain.model

enum class FileStatusType {
    MODIFIED,
    ADDED,
    DELETED,
    CONFLICTING
}

data class StatusFile(
    val path: String,
    val status: FileStatusType,
    val staged: Boolean
)
