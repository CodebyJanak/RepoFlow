package com.repoflow.core.domain.model

data class RepoSummary(
    val name: String,
    val description: String?,
    val primaryLanguage: String?,
    val totalCommits: Int,
    val totalBranches: Int,
    val totalContributors: Int,
    val recentActivity: String,
    val healthScore: Int,
    val insights: List<String> = emptyList()
)
