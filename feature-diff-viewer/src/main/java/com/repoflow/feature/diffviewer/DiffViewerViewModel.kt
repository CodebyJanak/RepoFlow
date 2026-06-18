package com.repoflow.feature.diffviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.DiffFile
import com.repoflow.core.domain.repository.GitRepository
import com.repoflow.core.ui.highlight.detectLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode { INLINE, SIDE_BY_SIDE }

data class DiffViewerUiState(
    val isLoading: Boolean = false,
    val diffFile: DiffFile? = null,
    val error: String? = null,
    val viewMode: ViewMode = ViewMode.INLINE,
    val filePath: String = "",
    val language: String = "",
    val staged: Boolean = false
)

@HiltViewModel
class DiffViewerViewModel @Inject constructor(
    private val gitRepository: GitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiffViewerUiState())
    val uiState: StateFlow<DiffViewerUiState> = _uiState.asStateFlow()

    fun loadDiff(localPath: String, filePath: String, staged: Boolean = false) {
        val language = detectLanguage(filePath)
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            filePath = filePath,
            language = language,
            staged = staged
        )

        viewModelScope.launch {
            gitRepository.getFileDiff(localPath, filePath, staged)
                .onSuccess { diffFile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        diffFile = diffFile
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load diff"
                    )
                }
        }
    }

    fun toggleViewMode() {
        val newMode = when (_uiState.value.viewMode) {
            ViewMode.INLINE -> ViewMode.SIDE_BY_SIDE
            ViewMode.SIDE_BY_SIDE -> ViewMode.INLINE
        }
        _uiState.value = _uiState.value.copy(viewMode = newMode)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
