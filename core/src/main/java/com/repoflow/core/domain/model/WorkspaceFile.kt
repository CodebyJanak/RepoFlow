package com.repoflow.core.domain.model

data class WorkspaceFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val gitStatus: String? = null
)
