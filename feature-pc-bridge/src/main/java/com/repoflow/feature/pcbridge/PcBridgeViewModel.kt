package com.repoflow.feature.pcbridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.ConnectionStatus
import com.repoflow.core.domain.model.PcCommand
import com.repoflow.core.domain.model.PcCommandResult
import com.repoflow.core.domain.model.PcConnection
import com.repoflow.core.domain.model.PcDevice
import com.repoflow.core.domain.model.PcFileEntry
import com.repoflow.core.domain.model.PcFileStatus
import com.repoflow.core.domain.model.PcGitCommit
import com.repoflow.core.domain.model.PcWorkspace
import com.repoflow.core.domain.repository.PcBridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PcBridgeUiState(
    val discoveredDevices: List<PcDevice> = emptyList(),
    val isScanning: Boolean = false,
    val connection: PcConnection = PcConnection(
        deviceId = "", deviceName = "", host = "", port = 0,
        authToken = null, status = ConnectionStatus.DISCONNECTED
    ),
    val error: String? = null
)

data class PcRemoteUiState(
    val isLoading: Boolean = true,
    val workspaces: List<PcWorkspace> = emptyList(),
    val selectedWorkspace: PcWorkspace? = null,
    val currentDirectory: String = "",
    val directoryEntries: List<PcFileEntry> = emptyList(),
    val isDirectoryLoading: Boolean = false,
    val branch: String = "",
    val ahead: Int = 0,
    val behind: Int = 0,
    val stagedFiles: List<PcFileStatus> = emptyList(),
    val unstagedFiles: List<PcFileStatus> = emptyList(),
    val untrackedFiles: List<String> = emptyList(),
    val conflicts: List<String> = emptyList(),
    val isGitLoading: Boolean = false,
    val recentCommits: List<PcGitCommit> = emptyList(),
    val isCommitsLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class PcBridgeViewModel @Inject constructor(
    private val repository: PcBridgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PcBridgeUiState())
    val uiState: StateFlow<PcBridgeUiState> = _uiState.asStateFlow()

    private val _remoteState = MutableStateFlow(PcRemoteUiState())
    val remoteState: StateFlow<PcRemoteUiState> = _remoteState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.connectionState.collect { connection ->
                _uiState.update { it.copy(connection = connection) }
                if (connection.status == ConnectionStatus.CONNECTED && connection.workspaces.isNotEmpty()) {
                    _remoteState.update { it.copy(workspaces = connection.workspaces, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            repository.discoveredDevices.collect { devices ->
                _uiState.update { it.copy(discoveredDevices = devices) }
            }
        }
    }

    fun startDiscovery() {
        _uiState.update { it.copy(isScanning = true, error = null) }
        viewModelScope.launch {
            repository.startDiscovery()
        }
    }

    fun stopDiscovery() {
        _uiState.update { it.copy(isScanning = false) }
        viewModelScope.launch {
            repository.stopDiscovery()
        }
    }

    fun connectToDevice(device: PcDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            val result = repository.connect(device)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun pairWithDevice(device: PcDevice, pairingCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            val result = repository.pair(
                device = device,
                pairingToken = pairingCode,
                deviceName = android.os.Build.MODEL,
                deviceId = android.os.Build.ID
            )
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    fun forgetDevice(device: PcDevice) {
        viewModelScope.launch {
            repository.forgetDevice(device.deviceId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
        _remoteState.update { it.copy(error = null) }
    }

    // Remote operations

    fun selectWorkspace(workspace: PcWorkspace) {
        _remoteState.update {
            it.copy(
                selectedWorkspace = workspace,
                currentDirectory = workspace.path
            )
        }
        loadGitStatus()
        loadDirectory(workspace.path)
    }

    fun loadDirectory(path: String) {
        _remoteState.update { it.copy(isDirectoryLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.executeCommand(PcCommand.ListDirectory(path)).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(isDirectoryLoading = false, error = "Command failed") } }
                is PcCommandResult.ListDirectoryResult -> {
                    _remoteState.update {
                        it.copy(
                            currentDirectory = result.path,
                            directoryEntries = result.entries,
                            isDirectoryLoading = false
                        )
                    }
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(isDirectoryLoading = false, error = "${result.code}: ${result.message}") }
                }
                else -> {
                    _remoteState.update { it.copy(isDirectoryLoading = false, error = "Unexpected response") }
                }
            }
        }
    }

    fun navigateIntoDirectory(entry: PcFileEntry) {
        if (entry.type == com.repoflow.core.domain.model.PcFileType.DIRECTORY) {
            loadDirectory(entry.path)
        }
    }

    fun navigateUp() {
        val current = _remoteState.value.currentDirectory
        if (current.isNotEmpty()) {
            val parent = current.substringBeforeLast("/", missingDelimiterValue = "/")
            if (parent.isNotEmpty()) {
                loadDirectory(parent)
            }
        }
    }

    fun loadGitStatus() {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        _remoteState.update { it.copy(isGitLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.executeCommand(PcCommand.GitStatus(workspace.id)).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(isGitLoading = false, error = "Command failed") } }
                is PcCommandResult.GitStatusResult -> {
                    _remoteState.update {
                        it.copy(
                            branch = result.branch,
                            ahead = result.ahead,
                            behind = result.behind,
                            stagedFiles = result.staged,
                            unstagedFiles = result.unstaged,
                            untrackedFiles = result.untracked,
                            conflicts = result.conflicts,
                            isGitLoading = false
                        )
                    }
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(isGitLoading = false, error = "${result.code}: ${result.message}") }
                }
                else -> {
                    _remoteState.update { it.copy(isGitLoading = false, error = "Unexpected response") }
                }
            }
        }
    }

    fun stageFiles(files: List<String>) {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        viewModelScope.launch {
            val result = repository.executeCommand(PcCommand.GitStage(workspace.id, files)).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(error = "Command failed") } }
                is PcCommandResult.GitStageResult -> {
                    if (result.success) loadGitStatus()
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(error = "${result.code}: ${result.message}") }
                }
                else -> {}
            }
        }
    }

    fun unstageFiles(files: List<String>) {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        viewModelScope.launch {
            val result = repository.executeCommand(PcCommand.GitUnstage(workspace.id, files)).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(error = "Command failed") } }
                is PcCommandResult.GitUnstageResult -> {
                    if (result.success) loadGitStatus()
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(error = "${result.code}: ${result.message}") }
                }
                else -> {}
            }
        }
    }

    fun commit(message: String) {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        viewModelScope.launch {
            _remoteState.update { it.copy(error = null, successMessage = null) }
            val result = repository.executeCommand(PcCommand.GitCommit(workspace.id, message)).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(error = "Command failed") } }
                is PcCommandResult.GitCommitResult -> {
                    if (result.success) {
                        _remoteState.update { it.copy(successMessage = "Committed ${result.commitHash.take(7)}") }
                        loadGitStatus()
                    }
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(error = "${result.code}: ${result.message}") }
                }
                else -> {}
            }
        }
    }

    fun push(force: Boolean = false) {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        viewModelScope.launch {
            _remoteState.update { it.copy(error = null, successMessage = null) }
            val result = repository.executeCommand(
                PcCommand.GitPush(workspace.id, branch = _remoteState.value.branch, force = force)
            ).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(error = "Command failed") } }
                is PcCommandResult.GitPushResult -> {
                    if (result.success) {
                        _remoteState.update { it.copy(successMessage = "Pushed ${result.pushedCommits} commits") }
                        loadGitStatus()
                    }
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(error = "${result.code}: ${result.message}") }
                }
                else -> {}
            }
        }
    }

    fun pull(rebase: Boolean = false) {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        viewModelScope.launch {
            _remoteState.update { it.copy(error = null, successMessage = null) }
            val result = repository.executeCommand(
                PcCommand.GitPull(workspace.id, branch = _remoteState.value.branch, rebase = rebase)
            ).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(error = "Command failed") } }
                is PcCommandResult.GitPullResult -> {
                    if (result.success) {
                        _remoteState.update { it.copy(successMessage = "Pulled ${result.commitsCount} commits") }
                        loadGitStatus()
                    }
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(error = "${result.code}: ${result.message}") }
                }
                else -> {}
            }
        }
    }

    fun fetch() {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        viewModelScope.launch {
            _remoteState.update { it.copy(error = null, successMessage = null) }
            val result = repository.executeCommand(PcCommand.GitFetch(workspace.id)).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(error = "Command failed") } }
                is PcCommandResult.GitFetchResult -> {
                    if (result.success) {
                        _remoteState.update { it.copy(successMessage = "Fetch complete") }
                    }
                }
                is PcCommandResult.Error -> {
                    _remoteState.update { it.copy(error = "${result.code}: ${result.message}") }
                }
                else -> {}
            }
        }
    }

    fun loadCommits() {
        val workspace = _remoteState.value.selectedWorkspace ?: return
        _remoteState.update { it.copy(isCommitsLoading = true) }
        viewModelScope.launch {
            val result = repository.executeCommand(PcCommand.ReadGitLog(workspace.id)).getOrNull()
            when (result) {
                null -> { _remoteState.update { it.copy(isCommitsLoading = false) } }
                is PcCommandResult.ReadGitLogResult -> {
                    _remoteState.update { it.copy(recentCommits = result.commits, isCommitsLoading = false) }
                }
                else -> {
                    _remoteState.update { it.copy(isCommitsLoading = false) }
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _remoteState.update { it.copy(successMessage = null) }
    }
}
