package com.repoflow.core.domain.model

data class User(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val name: String?,
    val email: String?,
    val bio: String?,
    val publicRepos: Int
)
