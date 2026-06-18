package com.repoflow.core.domain.model

data class Contributor(
    val login: String,
    val avatarUrl: String,
    val contributions: Int
)
