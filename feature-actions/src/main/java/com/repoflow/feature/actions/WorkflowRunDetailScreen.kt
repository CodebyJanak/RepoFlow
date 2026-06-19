package com.repoflow.feature.actions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.domain.model.Artifact
import com.repoflow.core.domain.model.JobStep
import com.repoflow.core.domain.model.RunConclusion
import com.repoflow.core.domain.model.RunStatus
import com.repoflow.core.domain.model.WorkflowJob
import com.repoflow.core.domain.model.WorkflowRun
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.LoadingShimmerList
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowRunDetailScreen(
    owner: String,
    name: String,
    runId: Long,
    onBack: () -> Unit,
    viewModel: ActionsViewModel = hiltViewModel()
) {
    val state by viewModel.runDetailState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(owner, name, runId) {
        viewModel.loadRunDetail(owner, name, runId)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RepoFlowTopAppBar(
                title = "Run #${state.run?.runNumber ?: runId}",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    LoadingShimmerList(
                        count = 4,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            state.error != null && state.run == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    EmptyState(
                        icon = Icons.Filled.Error,
                        title = "Failed to load run",
                        message = state.error ?: "Unknown error",
                        actionLabel = "Retry",
                        onAction = { viewModel.loadRunDetail(owner, name, runId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            state.run != null -> {
                RunDetailContent(
                    run = state.run!!,
                    jobs = state.jobs,
                    artifacts = state.artifacts,
                    isJobsLoading = state.isJobsLoading,
                    isArtifactsLoading = state.isArtifactsLoading,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun RunDetailContent(
    run: WorkflowRun,
    jobs: List<WorkflowJob>,
    artifacts: List<Artifact>,
    isJobsLoading: Boolean,
    isArtifactsLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item { RunHeader(run) }

        item { RunDetails(run) }

        item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest) }

        item {
            Text(
                text = "Jobs",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isJobsLoading) {
            item {
                LoadingShimmerList(count = 2, modifier = Modifier.padding(vertical = 4.dp))
            }
        } else if (jobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "No jobs found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(jobs, key = { it.id }) { job ->
                JobCard(job)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Artifacts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isArtifactsLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (artifacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "No artifacts for this run.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(artifacts, key = { it.id }) { artifact ->
                RunArtifactCard(artifact)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun RunHeader(run: WorkflowRun) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusIcon(
                    status = run.status,
                    conclusion = run.conclusion,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = run.displayTitle ?: run.name ?: "Run #${run.runNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = run.workflowName ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RunDetails(run: WorkflowRun) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DetailRow(label = "Status", value = run.status.apiValue.replaceFirstChar { it.uppercase() })
            DetailRow(label = "Conclusion", value = run.conclusion?.apiValue?.replaceFirstChar { it.uppercase() } ?: "-")
            DetailRow(label = "Branch", value = run.headBranch ?: "-")
            DetailRow(label = "Commit", value = run.headSha.take(7))
            DetailRow(label = "Run number", value = "#${run.runNumber}")
            DetailRow(label = "Attempt", value = "#${run.runAttempt}")
            if (run.actor != null) {
                DetailRow(label = "Triggered by", value = run.actor.login)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private val RunStatus.apiValue: String get() = name.lowercase()

@Composable
private fun StatusIcon(
    status: RunStatus,
    conclusion: RunConclusion?,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when {
        status.isActive -> Icons.Filled.PlayArrow to MaterialTheme.colorScheme.primary
        conclusion?.isSuccess == true -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.primary
        conclusion?.isFailure == true -> Icons.Filled.Error to MaterialTheme.colorScheme.error
        conclusion == RunConclusion.CANCELLED || conclusion == RunConclusion.SKIPPED ->
            Icons.Filled.Cancel to MaterialTheme.colorScheme.onSurfaceVariant
        else -> Icons.Filled.HourglassEmpty to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = tint,
        modifier = modifier
    )
}

@Composable
private fun JobCard(
    job: WorkflowJob,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                JobStatusIcon(
                    status = job.status,
                    conclusion = job.conclusion
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (job.conclusion != null) {
                        Text(
                            text = job.conclusion.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (job.steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                Spacer(modifier = Modifier.height(8.dp))
                job.steps.forEach { step ->
                    StepItem(step)
                }
            }
        }
    }
}

@Composable
private fun JobStatusIcon(
    status: String?,
    conclusion: String?
) {
    val (icon, tint) = when (status) {
        "completed" -> when (conclusion) {
            "success" -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.primary
            "failure" -> Icons.Filled.Error to MaterialTheme.colorScheme.error
            "cancelled" -> Icons.Filled.Cancel to MaterialTheme.colorScheme.onSurfaceVariant
            "skipped" -> Icons.Filled.HourglassEmpty to MaterialTheme.colorScheme.onSurfaceVariant
            else -> Icons.Filled.HourglassEmpty to MaterialTheme.colorScheme.onSurfaceVariant
        }
        "in_progress", "queued", "pending" -> Icons.Filled.PlayArrow to MaterialTheme.colorScheme.primary
        else -> Icons.Filled.HourglassEmpty to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Icon(
        imageVector = icon,
        contentDescription = status,
        tint = tint,
        modifier = Modifier.size(18.dp)
    )
}

@Composable
private fun StepItem(
    step: JobStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JobStatusIcon(
            status = step.status,
            conclusion = step.conclusion
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = step.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (step.conclusion != null) {
            Text(
                text = step.conclusion.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = when (step.conclusion) {
                    "success" -> MaterialTheme.colorScheme.primary
                    "failure" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun RunArtifactCard(
    artifact: Artifact,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Archive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artifact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artifact.sizeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (artifact.expired) {
                Text(
                    text = "Expired",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
