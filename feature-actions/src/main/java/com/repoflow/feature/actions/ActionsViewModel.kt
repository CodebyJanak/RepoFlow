package com.repoflow.feature.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.Artifact
import com.repoflow.core.domain.model.Workflow
import com.repoflow.core.domain.model.WorkflowJob
import com.repoflow.core.domain.model.WorkflowRun
import com.repoflow.core.domain.repository.ActionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActionsDashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val workflows: List<Workflow> = emptyList(),
    val recentRuns: List<WorkflowRun> = emptyList(),
    val artifacts: List<Artifact> = emptyList(),
    val isWorkflowsLoading: Boolean = false,
    val isRunsLoading: Boolean = false,
    val isArtifactsLoading: Boolean = false
)

data class RunDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val run: WorkflowRun? = null,
    val jobs: List<WorkflowJob> = emptyList(),
    val artifacts: List<Artifact> = emptyList(),
    val isJobsLoading: Boolean = false,
    val isArtifactsLoading: Boolean = false
)

@HiltViewModel
class ActionsViewModel @Inject constructor(
    private val actionsRepository: ActionsRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(ActionsDashboardState())
    val dashboardState: StateFlow<ActionsDashboardState> = _dashboardState.asStateFlow()

    private val _runDetailState = MutableStateFlow(RunDetailState())
    val runDetailState: StateFlow<RunDetailState> = _runDetailState.asStateFlow()

    fun loadDashboard(owner: String, repo: String) {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val workflowsResult = actionsRepository.getWorkflows(owner, repo)
            workflowsResult.onSuccess { workflows ->
                _dashboardState.value = _dashboardState.value.copy(
                    workflows = workflows,
                    isWorkflowsLoading = false
                )
            }
            workflowsResult.onFailure { e ->
                _dashboardState.value = _dashboardState.value.copy(
                    error = e.message,
                    isWorkflowsLoading = false
                )
            }
        }
        loadRuns(owner, repo)
        loadArtifacts(owner, repo)
    }

    fun loadRuns(owner: String, repo: String) {
        _dashboardState.value = _dashboardState.value.copy(isRunsLoading = true)
        viewModelScope.launch {
            val result = actionsRepository.getRepositoryRuns(owner, repo)
            result.onSuccess { runs ->
                _dashboardState.value = _dashboardState.value.copy(
                    recentRuns = runs,
                    isRunsLoading = false,
                    isLoading = false
                )
            }
            result.onFailure { e ->
                _dashboardState.value = _dashboardState.value.copy(
                    error = e.message,
                    isRunsLoading = false,
                    isLoading = false
                )
            }
        }
    }

    fun loadArtifacts(owner: String, repo: String) {
        _dashboardState.value = _dashboardState.value.copy(isArtifactsLoading = true)
        viewModelScope.launch {
            val result = actionsRepository.getArtifacts(owner, repo)
            result.onSuccess { artifacts ->
                _dashboardState.value = _dashboardState.value.copy(
                    artifacts = artifacts,
                    isArtifactsLoading = false
                )
            }
            result.onFailure {
                _dashboardState.value = _dashboardState.value.copy(isArtifactsLoading = false)
            }
        }
    }

    fun loadRunDetail(owner: String, repo: String, runId: Long) {
        _runDetailState.value = _runDetailState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = actionsRepository.getWorkflowRun(owner, repo, runId)
            result.onSuccess { run ->
                _runDetailState.value = _runDetailState.value.copy(run = run, isLoading = false)
                loadJobs(owner, repo, runId)
                loadRunArtifacts(owner, repo, runId)
            }
            result.onFailure { e ->
                _runDetailState.value = _runDetailState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadJobs(owner: String, repo: String, runId: Long) {
        _runDetailState.value = _runDetailState.value.copy(isJobsLoading = true)
        viewModelScope.launch {
            val result = actionsRepository.getWorkflowRunJobs(owner, repo, runId)
            result.onSuccess { jobs ->
                _runDetailState.value = _runDetailState.value.copy(jobs = jobs, isJobsLoading = false)
            }
            result.onFailure {
                _runDetailState.value = _runDetailState.value.copy(isJobsLoading = false)
            }
        }
    }

    fun loadRunArtifacts(owner: String, repo: String, runId: Long) {
        _runDetailState.value = _runDetailState.value.copy(isArtifactsLoading = true)
        viewModelScope.launch {
            val result = actionsRepository.getRunArtifacts(owner, repo, runId)
            result.onSuccess { artifacts ->
                _runDetailState.value = _runDetailState.value.copy(artifacts = artifacts, isArtifactsLoading = false)
            }
            result.onFailure {
                _runDetailState.value = _runDetailState.value.copy(isArtifactsLoading = false)
            }
        }
    }
}
