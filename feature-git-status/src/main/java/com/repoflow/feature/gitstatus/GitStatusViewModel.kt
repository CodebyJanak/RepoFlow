package com.repoflow.feature.gitstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.FileStatusType
import com.repoflow.core.domain.model.StatusFile
import com.repoflow.core.domain.repository.GitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GitStatusUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val modifiedFiles: List<StatusFile> = emptyList(),
    val newFiles: List<StatusFile> = emptyList(),
    val deletedFiles: List<StatusFile> = emptyList(),
    val conflictingFiles: List<StatusFile> = emptyList(),
    val stagedCount: Int = 0,
    val unstagedCount: Int = 0,
    val isStageAllLoading: Boolean = false,
    val isUnstageAllLoading: Boolean = false,
    val stagedFilePaths: Set<String> = emptySet(),
    val successMessage: String? = null
)

@HiltViewModel
class GitStatusViewModel @Inject constructor(
    private val gitRepository: GitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GitStatusUiState())
    val uiState: StateFlow<GitStatusUiState> = _uiState.asStateFlow()

    private var localPath: String = ""

    fun loadStatus(localPath: String) {
        this.localPath = localPath
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
        viewModelScope.launch {
            val result = gitRepository.getStatus(localPath)
            result.onSuccess { files -> processStatus(files) }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load status",
                    isLoading = false
                )
            }
        }
    }

    private fun processStatus(files: List<StatusFile>) {
        val modified = files.filter { it.status == FileStatusType.MODIFIED }
        val added = files.filter { it.status == FileStatusType.ADDED }
        val deleted = files.filter { it.status == FileStatusType.DELETED }
        val conflicting = files.filter { it.status == FileStatusType.CONFLICTING }
        val stagedPaths = files.filter { it.staged }.map { it.path }.toSet()

        _uiState.value = _uiState.value.copy(
            modifiedFiles = modified,
            newFiles = added,
            deletedFiles = deleted,
            conflictingFiles = conflicting,
            stagedFilePaths = stagedPaths,
            stagedCount = files.count { it.staged },
            unstagedCount = files.count { !it.staged },
            isLoading = false
        )
    }

    fun stageFile(filePath: String) {
        viewModelScope.launch {
            val result = gitRepository.stageFile(localPath, filePath)
            result.onSuccess { refresh() }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun unstageFile(filePath: String) {
        viewModelScope.launch {
            val result = gitRepository.unstageFile(localPath, filePath)
            result.onSuccess { refresh() }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun stageAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStageAllLoading = true)
            val result = gitRepository.stageAll(localPath)
            result.onSuccess {
                refresh()
                _uiState.value = _uiState.value.copy(successMessage = "All files staged")
            }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isStageAllLoading = false
                )
            }
        }
    }

    fun unstageAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUnstageAllLoading = true)
            val result = gitRepository.unstageAll(localPath)
            result.onSuccess {
                refresh()
                _uiState.value = _uiState.value.copy(successMessage = "All files unstaged")
            }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isUnstageAllLoading = false
                )
            }
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun refresh() {
        loadStatus(localPath)
    }
}
