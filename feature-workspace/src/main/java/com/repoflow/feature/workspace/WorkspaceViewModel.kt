package com.repoflow.feature.workspace

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.WorkspaceFile
import com.repoflow.core.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Breadcrumb(
    val name: String,
    val uri: Uri
)

data class WorkspaceUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val rootUri: String? = null,
    val currentFolderUri: Uri? = null,
    val files: List<WorkspaceFile> = emptyList(),
    val folderStack: List<Breadcrumb> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<WorkspaceFile> = emptyList(),
    val isSearching: Boolean = false,
    val isGitRepository: Boolean = false
)

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    init {
        restoreRootUri()
    }

    private fun restoreRootUri() {
        val savedUri = repository.getRootWorkspaceUri()
        if (savedUri != null) {
            val uri = Uri.parse(savedUri)
            val name = displayNameFromUri(uri)
            _uiState.value = _uiState.value.copy(
                rootUri = savedUri,
                folderStack = listOf(Breadcrumb(name, uri)),
                isLoading = false
            )
            navigateToFolder(uri)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onRootUriSelected(uriString: String) {
        val uri = Uri.parse(uriString)
        repository.setRootWorkspaceUri(uriString)
        val name = displayNameFromUri(uri)
        _uiState.value = _uiState.value.copy(
            rootUri = uriString,
            folderStack = listOf(Breadcrumb(name, uri)),
            error = null,
            isLoading = true
        )
        navigateToFolder(uri)
    }

    fun openFolder(uri: Uri, name: String) {
        val stack = _uiState.value.folderStack + Breadcrumb(name, uri)
        _uiState.value = _uiState.value.copy(folderStack = stack)
        navigateToFolder(uri)
    }

    fun navigateUp() {
        val stack = _uiState.value.folderStack
        if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            _uiState.value = _uiState.value.copy(folderStack = newStack)
            navigateToFolder(newStack.last().uri)
        }
    }

    fun navigateToBreadcrumb(index: Int) {
        val stack = _uiState.value.folderStack
        if (index in stack.indices) {
            val newStack = stack.take(index + 1)
            _uiState.value = _uiState.value.copy(folderStack = newStack)
            navigateToFolder(newStack.last().uri)
        }
    }

    private fun navigateToFolder(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                currentFolderUri = uri
            )

            val filesResult = repository.getWorkspaceFiles(uri)
            filesResult.onSuccess { files ->
                val repoResult = repository.isRepository(uri)
                val isRepo = repoResult.getOrElse { false }

                _uiState.value = _uiState.value.copy(
                    files = files,
                    isGitRepository = isRepo,
                    isLoading = false
                )
            }
            filesResult.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load files",
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        val currentUri = _uiState.value.currentFolderUri ?: return
        navigateToFolder(currentUri)
    }

    fun search(query: String) {
        val currentUri = _uiState.value.currentFolderUri ?: return
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(isSearching = false, searchQuery = "", searchResults = emptyList())
            return
        }
        _uiState.value = _uiState.value.copy(isSearching = true, searchQuery = query)
        viewModelScope.launch {
            val result = repository.searchFiles(query, currentUri)
            result.onSuccess { files ->
                _uiState.value = _uiState.value.copy(searchResults = files)
            }
            result.onFailure {
                _uiState.value = _uiState.value.copy(searchResults = emptyList())
            }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(isSearching = false, searchQuery = "", searchResults = emptyList())
    }

    fun createFolder(name: String) {
        val currentUri = _uiState.value.currentFolderUri ?: return
        viewModelScope.launch {
            val result = repository.createDirectory(currentUri, name)
            result.onSuccess { refresh() }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteItem(uri: Uri, isDirectory: Boolean) {
        viewModelScope.launch {
            val result = if (isDirectory) {
                repository.deleteDirectory(uri)
            } else {
                repository.deleteFile(uri)
            }
            result.onSuccess { refresh() }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun displayNameFromUri(uri: Uri): String {
        val path = uri.lastPathSegment ?: return "Workspace"
        return Uri.decode(path.substringAfter(':')).ifBlank { "Workspace" }
    }
}
