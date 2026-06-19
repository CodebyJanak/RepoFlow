package com.repoflow.core.data.remote.pc

import com.repoflow.core.domain.model.PcCommand
import com.repoflow.core.domain.model.PcCommandResult
import com.repoflow.core.domain.model.PcDevice
import com.repoflow.core.domain.model.PcFileEntry
import com.repoflow.core.domain.model.PcFileStatus
import com.repoflow.core.domain.model.PcFileType
import com.repoflow.core.domain.model.PcGitCommit
import com.repoflow.core.domain.model.PcWorkspace
import java.util.UUID

object PcMessageMapper {

    private fun generateId(): String = "req_${UUID.randomUUID().toString().take(8)}"

    fun toRequest(command: PcCommand, authToken: String? = null): PcRequestMessage {
        val (cmdName, params) = when (command) {
            is PcCommand.Pair -> "pair" to mapOf(
                "method" to command.method,
                "pairingToken" to command.pairingToken,
                "deviceName" to command.deviceName,
                "deviceId" to command.deviceId
            )
            is PcCommand.ListDirectory -> "list_directory" to mapOf("path" to command.path)
            is PcCommand.ReadFile -> "read_file" to mapOf("path" to command.path)
            is PcCommand.GitStatus -> "git_status" to mapOf("workspaceId" to command.workspaceId)
            is PcCommand.GitStage -> "git_stage" to mapOf(
                "workspaceId" to command.workspaceId,
                "files" to command.files
            )
            is PcCommand.GitUnstage -> "git_unstage" to mapOf(
                "workspaceId" to command.workspaceId,
                "files" to command.files
            )
            is PcCommand.GitCommit -> {
                val params = mutableMapOf<String, Any>(
                    "workspaceId" to command.workspaceId,
                    "message" to command.message
                )
                command.authorName?.let { params["authorName"] = it }
                command.authorEmail?.let { params["authorEmail"] = it }
                "git_commit" to params
            }
            is PcCommand.GitPush -> {
                val params = mutableMapOf<String, Any>(
                    "workspaceId" to command.workspaceId,
                    "remote" to command.remote,
                    "force" to command.force
                )
                command.branch?.let { params["branch"] = it }
                "git_push" to params
            }
            is PcCommand.GitPull -> {
                val params = mutableMapOf<String, Any>(
                    "workspaceId" to command.workspaceId,
                    "remote" to command.remote,
                    "rebase" to command.rebase
                )
                command.branch?.let { params["branch"] = it }
                "git_pull" to params
            }
            is PcCommand.GitFetch -> "git_fetch" to mapOf(
                "workspaceId" to command.workspaceId,
                "remote" to command.remote
            )
            is PcCommand.ListWorkspaces -> "list_workspaces" to emptyMap()
            is PcCommand.ReadGitLog -> "read_git_log" to mapOf(
                "workspaceId" to command.workspaceId,
                "maxCount" to command.maxCount
            )
            is PcCommand.Ping -> "ping" to emptyMap()
            is PcCommand.Disconnect -> "disconnect" to mapOf("reason" to command.reason)
        }
        return PcRequestMessage(
            id = generateId(),
            command = cmdName,
            params = params,
            authToken = authToken
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun toResult(response: PcResponseMessage, command: PcCommand): PcCommandResult {
        if (!response.success) {
            return PcCommandResult.Error(
                code = response.error ?: "UNKNOWN_ERROR",
                message = response.message ?: "Unknown error"
            )
        }

        val data = response.data ?: return PcCommandResult.Error("EMPTY_RESPONSE", "Empty response data")

        return when (command) {
            is PcCommand.Pair -> PcCommandResult.PairResult(
                authToken = data["authToken"] as? String ?: "",
                workspaces = (data["workspaces"] as? List<Map<String, Any?>>)?.map { it.toWorkspace() }
                    ?: emptyList(),
                agentVersion = data["agentVersion"] as? String ?: ""
            )
            is PcCommand.ListDirectory -> PcCommandResult.ListDirectoryResult(
                path = data["path"] as? String ?: "",
                entries = (data["entries"] as? List<Map<String, Any?>>)?.map { it.toFileEntry() }
                    ?: emptyList()
            )
            is PcCommand.ReadFile -> PcCommandResult.ReadFileResult(
                path = data["path"] as? String ?: "",
                content = data["content"] as? String ?: "",
                size = (data["size"] as? Number)?.toLong() ?: 0L,
                encoding = data["encoding"] as? String ?: "base64"
            )
            is PcCommand.GitStatus -> PcCommandResult.GitStatusResult(
                branch = data["branch"] as? String ?: "",
                ahead = (data["ahead"] as? Number)?.toInt() ?: 0,
                behind = (data["behind"] as? Number)?.toInt() ?: 0,
                hasUncommitted = data["hasUncommitted"] as? Boolean ?: false,
                staged = (data["staged"] as? List<Map<String, Any?>>)?.map { it.toFileStatus() }
                    ?: emptyList(),
                unstaged = (data["unstaged"] as? List<Map<String, Any?>>)?.map { it.toFileStatus() }
                    ?: emptyList(),
                untracked = (data["untracked"] as? List<String>) ?: emptyList(),
                conflicts = (data["conflicts"] as? List<String>) ?: emptyList()
            )
            is PcCommand.GitStage -> PcCommandResult.GitStageResult(
                success = data["success"] as? Boolean ?: false,
                stagedCount = (data["stagedCount"] as? Number)?.toInt() ?: 0
            )
            is PcCommand.GitUnstage -> PcCommandResult.GitUnstageResult(
                success = data["success"] as? Boolean ?: false,
                unstagedCount = (data["unstagedCount"] as? Number)?.toInt() ?: 0
            )
            is PcCommand.GitCommit -> PcCommandResult.GitCommitResult(
                success = data["success"] as? Boolean ?: false,
                commitHash = data["commitHash"] as? String ?: "",
                branch = data["branch"] as? String ?: ""
            )
            is PcCommand.GitPush -> PcCommandResult.GitPushResult(
                success = data["success"] as? Boolean ?: false,
                pushedCommits = (data["pushedCommits"] as? Number)?.toInt() ?: 0,
                remoteBranch = data["remoteBranch"] as? String ?: ""
            )
            is PcCommand.GitPull -> PcCommandResult.GitPullResult(
                success = data["success"] as? Boolean ?: false,
                updated = data["updated"] as? Boolean ?: false,
                commitsCount = (data["commitsCount"] as? Number)?.toInt() ?: 0
            )
            is PcCommand.GitFetch -> PcCommandResult.GitFetchResult(
                success = data["success"] as? Boolean ?: false,
                fetched = data["fetched"] as? Boolean ?: false
            )
            is PcCommand.ListWorkspaces -> PcCommandResult.ListWorkspacesResult(
                workspaces = (data["workspaces"] as? List<Map<String, Any?>>)?.map { it.toWorkspace() }
                    ?: emptyList()
            )
            is PcCommand.ReadGitLog -> PcCommandResult.ReadGitLogResult(
                commits = (data["commits"] as? List<Map<String, Any?>>)?.map { it.toGitCommit() }
                    ?: emptyList()
            )
            is PcCommand.Ping -> PcCommandResult.PingResult(
                timestamp = data["timestamp"] as? String ?: "",
                agentVersion = data["agentVersion"] as? String ?: ""
            )
            is PcCommand.Disconnect -> PcCommandResult.DisconnectResult(
                success = data["success"] as? Boolean ?: false
            )
        }
    }

    fun toDevice(beacon: PcBeaconMessage, host: String): PcDevice = PcDevice(
        deviceId = beacon.deviceId,
        deviceName = beacon.deviceName,
        host = host,
        port = beacon.port,
        protocolVersion = beacon.protocolVersion,
        requiresPairing = beacon.requiresPairing
    )

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toFileEntry(): PcFileEntry = PcFileEntry(
        name = this["name"] as? String ?: "",
        type = PcFileType.fromString(this["type"] as? String ?: "file"),
        path = this["path"] as? String ?: "",
        size = (this["size"] as? Number)?.toLong(),
        modifiedAt = this["modifiedAt"] as? String
    )

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toFileStatus(): PcFileStatus = PcFileStatus(
        path = this["path"] as? String ?: "",
        status = this["status"] as? String ?: ""
    )

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toWorkspace(): PcWorkspace = PcWorkspace(
        id = this["id"] as? String ?: "",
        name = this["name"] as? String ?: "",
        path = this["path"] as? String ?: "",
        currentBranch = this["currentBranch"] as? String,
        isDirty = this["isDirty"] as? Boolean ?: false
    )

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toGitCommit(): PcGitCommit = PcGitCommit(
        hash = this["hash"] as? String ?: "",
        author = this["author"] as? String ?: "",
        message = this["message"] as? String ?: "",
        timestamp = this["timestamp"] as? String ?: ""
    )
}
