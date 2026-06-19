package com.repoflow.core.domain.model

data class CommitSuggestion(
    val type: CommitType,
    val scope: String?,
    val summary: String,
    val body: String?,
    val breakingChange: Boolean,
    val fullMessage: String
)

enum class CommitType(val label: String) {
    FEAT("feat"),
    FIX("fix"),
    DOCS("docs"),
    STYLE("style"),
    REFACTOR("refactor"),
    PERF("perf"),
    TEST("test"),
    BUILD("build"),
    CI("ci"),
    CHORE("chore"),
    REVERT("revert");

    companion object {
        fun fromLabel(label: String): CommitType? = entries.find { it.label == label }
    }
}
