package com.repoflow.core.domain.model

data class GitRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val isPrivate: Boolean,
    val stars: Int,
    val forks: Int,
    val openIssues: Int,
    val defaultBranch: String,
    val owner: User,
    val isCloned: Boolean = false,
    val localPath: String? = null
)
