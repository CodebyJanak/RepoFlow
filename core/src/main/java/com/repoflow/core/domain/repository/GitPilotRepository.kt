package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.ChangelogResult
import com.repoflow.core.domain.model.CommitSuggestion
import com.repoflow.core.domain.model.ConflictAnalysis
import com.repoflow.core.domain.model.GitErrorAnalysis
import com.repoflow.core.domain.model.RepoSummary

interface GitPilotRepository {

    suspend fun generateCommitMessage(diff: String): Result<CommitSuggestion>

    suspend fun generateChangelog(gitLog: String): Result<ChangelogResult>

    suspend fun explainGitError(errorMessage: String): Result<GitErrorAnalysis>

    suspend fun explainMergeConflict(conflictDiff: String): Result<ConflictAnalysis>

    suspend fun summarizeRepository(
        name: String,
        description: String?,
        language: String?,
        commitCount: Int,
        branchCount: Int,
        contributorCount: Int,
        recentCommits: String
    ): Result<RepoSummary>
}
