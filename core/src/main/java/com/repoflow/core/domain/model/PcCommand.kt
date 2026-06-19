package com.repoflow.core.domain.model

sealed class PcCommand {
    data class Pair(
        val method: String,
        val pairingToken: String,
        val deviceName: String,
        val deviceId: String
    ) : PcCommand()

    data class ListDirectory(val path: String) : PcCommand()
    data class ReadFile(val path: String) : PcCommand()

    data class GitStatus(val workspaceId: String) : PcCommand()
    data class GitStage(val workspaceId: String, val files: List<String>) : PcCommand()
    data class GitUnstage(val workspaceId: String, val files: List<String>) : PcCommand()
    data class GitCommit(
        val workspaceId: String,
        val message: String,
        val authorName: String? = null,
        val authorEmail: String? = null
    ) : PcCommand()

    data class GitPush(
        val workspaceId: String,
        val remote: String = "origin",
        val branch: String? = null,
        val force: Boolean = false
    ) : PcCommand()

    data class GitPull(
        val workspaceId: String,
        val remote: String = "origin",
        val branch: String? = null,
        val rebase: Boolean = false
    ) : PcCommand()

    data class GitFetch(
        val workspaceId: String,
        val remote: String = "origin"
    ) : PcCommand()

    data object ListWorkspaces : PcCommand()
    data class ReadGitLog(val workspaceId: String, val maxCount: Int = 20) : PcCommand()
    data object Ping : PcCommand()
    data class Disconnect(val reason: String = "user_initiated") : PcCommand()
}
