package com.repoflow.core.domain.model

data class ConflictAnalysis(
    val conflictFiles: List<ConflictFile> = emptyList(),
    val summary: String,
    val totalConflicts: Int
)

data class ConflictFile(
    val path: String,
    val conflictCount: Int,
    val explanation: String,
    val suggestedResolution: String
)
