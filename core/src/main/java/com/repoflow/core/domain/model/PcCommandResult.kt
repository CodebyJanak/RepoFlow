package com.repoflow.core.domain.model

sealed class PcCommandResult {
    data class PairResult(
        val authToken: String,
        val workspaces: List<PcWorkspace>,
        val agentVersion: String
    ) : PcCommandResult()

    data class ListDirectoryResult(
        val path: String,
        val entries: List<PcFileEntry>
    ) : PcCommandResult()

    data class ReadFileResult(
        val path: String,
        val content: String,
        val size: Long,
        val encoding: String
    ) : PcCommandResult()

    data class GitStatusResult(
        val branch: String,
        val ahead: Int,
        val behind: Int,
        val hasUncommitted: Boolean,
        val staged: List<PcFileStatus>,
        val unstaged: List<PcFileStatus>,
        val untracked: List<String>,
        val conflicts: List<String>
    ) : PcCommandResult()

    data class GitStageResult(
        val success: Boolean,
        val stagedCount: Int
    ) : PcCommandResult()

    data class GitUnstageResult(
        val success: Boolean,
        val unstagedCount: Int
    ) : PcCommandResult()

    data class GitCommitResult(
        val success: Boolean,
        val commitHash: String,
        val branch: String
    ) : PcCommandResult()

    data class GitPushResult(
        val success: Boolean,
        val pushedCommits: Int,
        val remoteBranch: String
    ) : PcCommandResult()

    data class GitPullResult(
        val success: Boolean,
        val updated: Boolean,
        val commitsCount: Int
    ) : PcCommandResult()

    data class GitFetchResult(val success: Boolean, val fetched: Boolean) : PcCommandResult()

    data class ListWorkspacesResult(val workspaces: List<PcWorkspace>) : PcCommandResult()
    data class ReadGitLogResult(val commits: List<PcGitCommit>) : PcCommandResult()
    data class PingResult(val timestamp: String, val agentVersion: String) : PcCommandResult()
    data class DisconnectResult(val success: Boolean) : PcCommandResult()
    data class Error(val code: String, val message: String) : PcCommandResult()
}

data class PcFileStatus(
    val path: String,
    val status: String
)

data class PcGitCommit(
    val hash: String,
    val author: String,
    val message: String,
    val timestamp: String
)
