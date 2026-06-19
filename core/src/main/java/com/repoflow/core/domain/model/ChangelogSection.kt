package com.repoflow.core.domain.model

data class ChangelogSection(
    val version: String,
    val date: String,
    val features: List<String> = emptyList(),
    val bugFixes: List<String> = emptyList(),
    val performance: List<String> = emptyList(),
    val refactors: List<String> = emptyList(),
    val documentation: List<String> = emptyList(),
    val breakingChanges: List<String> = emptyList(),
    val other: List<String> = emptyList()
)

data class ChangelogResult(
    val sections: List<ChangelogSection>,
    val markdown: String
)
