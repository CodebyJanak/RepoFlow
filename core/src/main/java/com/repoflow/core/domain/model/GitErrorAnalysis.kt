package com.repoflow.core.domain.model

data class GitErrorAnalysis(
    val errorType: String,
    val summary: String,
    val cause: String,
    val solution: String,
    val commands: List<String> = emptyList(),
    val prevention: String? = null
)
