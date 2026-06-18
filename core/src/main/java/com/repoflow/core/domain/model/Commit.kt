package com.repoflow.core.domain.model

data class Commit(
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long
)
