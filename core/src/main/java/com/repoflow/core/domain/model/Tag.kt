package com.repoflow.core.domain.model

data class Tag(
    val name: String,
    val commitHash: String,
    val message: String? = null
)
