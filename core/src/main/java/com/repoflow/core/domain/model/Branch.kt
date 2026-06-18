package com.repoflow.core.domain.model

data class Branch(
    val name: String,
    val sha: String,
    val isProtected: Boolean
)
