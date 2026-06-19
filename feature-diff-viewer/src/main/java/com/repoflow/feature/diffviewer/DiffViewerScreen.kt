package com.repoflow.feature.diffviewer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.domain.model.DiffFile
import com.repoflow.core.domain.model.DiffHunk
import com.repoflow.core.domain.model.DiffLine
import com.repoflow.core.domain.model.DiffLineType
import com.repoflow.core.ui.highlight.highlightLine
import com.repoflow.core.ui.components.LoadingShimmerList

private val addedBg = Color(0xFF064E3B)
private val addedLineBg = Color(0xFF064E3B)
private val removedBg = Color(0xFF7F1D1D)
private val removedLineBg = Color(0xFF7F1D1D)
private val modifiedBg = Color(0xFF1E3A5F)
private val lineNumFg = Color(0xFF6B7280)
private val lineNumBg = Color(0xFF1F2937)
private val hunkHeaderBg = Color(0xFF1E3A5F)
private val hunkHeaderFg = Color(0xFF93C5FD)
private val indicatorFg = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffViewerScreen(
    localPath: String,
    filePath: String,
    staged: Boolean = false,
    onBack: () -> Unit,
    viewModel: DiffViewerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(localPath, filePath, staged) {
        viewModel.loadDiff(localPath, filePath, staged)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = filePath.substringAfterLast('/'),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        if (state.diffFile != null) {
                            Text(
                                text = filePath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.INLINE)
                                Icons.Filled.ViewColumn else Icons.Filled.ViewList,
                            contentDescription = "Toggle view mode",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (state.viewMode == ViewMode.INLINE) "Side-by-side" else "Inline",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingShimmerList(
                    count = 10,
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            state.error != null -> {
                ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.loadDiff(localPath, filePath, staged) },
                    modifier = Modifier.padding(padding)
                )
            }

            state.diffFile == null || state.diffFile!!.isEmpty -> {
                EmptyDiffState(
                    staged = state.staged,
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                DiffContent(
                    diffFile = state.diffFile!!,
                    language = state.language,
                    viewMode = state.viewMode,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyDiffState(staged: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (staged) "No staged changes for this file" else "No unstaged changes for this file",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DiffContent(
    diffFile: DiffFile,
    language: String,
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        DiffStatsBar(diffFile)

        Divider()

        if (viewMode == ViewMode.INLINE) {
            InlineDiffView(diffFile, language)
        } else {
            SideBySideDiffView(diffFile, language)
        }
    }
}

@Composable
private fun DiffStatsBar(diffFile: DiffFile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = diffFile.oldPath.split("/").lastOrNull() ?: diffFile.oldPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+${diffFile.addedLines}",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "-${diffFile.removedLines}",
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${diffFile.hunks.size} hunk${if (diffFile.hunks.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InlineDiffView(diffFile: DiffFile, language: String) {
    val horizontalScroll = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScroll)
    ) {
        for ((hunkIndex, hunk) in diffFile.hunks.withIndex()) {
            item(key = "hunk_${hunkIndex}_header") {
                HunkHeader(hunk)
            }
            itemsIndexed(
                items = hunk.lines,
                key = { index, _ -> "hunk_${hunkIndex}_line_$index" }
            ) { _, line ->
                InlineDiffLine(line, language)
            }
        }
    }
}

@Composable
private fun HunkHeader(hunk: DiffHunk) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(hunkHeaderBg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "@@ -${hunk.oldStart},${hunk.oldCount} +${hunk.newStart},${hunk.newCount} @@",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = hunkHeaderFg
        )
    }
}

@Composable
private fun InlineDiffLine(line: DiffLine, language: String) {
    val bgColor by animateColorAsState(
        targetValue = when (line.type) {
            DiffLineType.ADDED -> addedLineBg
            DiffLineType.REMOVED -> removedLineBg
            DiffLineType.UNCHANGED -> Color.Transparent
        },
        label = "lineBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .height(IntrinsicSize.Min)
    ) {
        LineNumberCell(
            number = line.oldLineNum,
            modifier = Modifier.width(48.dp)
        )
        LineNumberCell(
            number = line.newLineNum,
            modifier = Modifier.width(48.dp)
        )
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight()
                .padding(start = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when (line.type) {
                    DiffLineType.ADDED -> "+"
                    DiffLineType.REMOVED -> "-"
                    DiffLineType.UNCHANGED -> " "
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = indicatorFg
            )
        }
        BasicText(
            text = highlightLine(line.content, language),
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        )
    }
}

@Composable
private fun LineNumberCell(number: Int?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(lineNumBg)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = number?.toString() ?: "",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = lineNumFg,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
private fun SideBySideDiffView(diffFile: DiffFile, language: String) {
    val rows = remember(diffFile) { buildSideBySideRows(diffFile) }
    val horizontalScroll = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScroll)
    ) {
        for ((hunkIndex, hunk) in diffFile.hunks.withIndex()) {
            item(key = "sb_hunk_${hunkIndex}_header") {
                HunkHeader(hunk)
            }

            val hunkRows = remember(hunk) { buildHunkSideBySideRows(hunk) }

            itemsIndexed(
                items = hunkRows,
                key = { index, _ -> "sb_hunk_${hunkIndex}_row_$index" }
            ) { _, row ->
                SideBySideRow(row, language)
            }
        }
    }
}

private data class SideBySideRowData(
    val oldLineNum: Int?,
    val newLineNum: Int?,
    val oldContent: String?,
    val newContent: String?,
    val type: SideBySideType
)

private enum class SideBySideType { UNCHANGED, ADDED, REMOVED, MODIFIED }

private fun buildSideBySideRows(diffFile: DiffFile): List<SideBySideRowData> {
    return diffFile.hunks.flatMap { buildHunkSideBySideRows(it) }
}

private fun buildHunkSideBySideRows(hunk: DiffHunk): List<SideBySideRowData> {
    val rows = mutableListOf<SideBySideRowData>()
    var i = 0
    while (i < hunk.lines.size) {
        val line = hunk.lines[i]
        when (line.type) {
            DiffLineType.UNCHANGED -> {
                rows.add(
                    SideBySideRowData(
                        line.oldLineNum, line.newLineNum,
                        line.content, line.content,
                        SideBySideType.UNCHANGED
                    )
                )
            }
            DiffLineType.REMOVED -> {
                val next = hunk.lines.getOrNull(i + 1)
                if (next?.type == DiffLineType.ADDED) {
                    rows.add(
                        SideBySideRowData(
                            line.oldLineNum, next.newLineNum,
                            line.content, next.content,
                            SideBySideType.MODIFIED
                        )
                    )
                    i++
                } else {
                    rows.add(
                        SideBySideRowData(
                            line.oldLineNum, null,
                            line.content, null,
                            SideBySideType.REMOVED
                        )
                    )
                }
            }
            DiffLineType.ADDED -> {
                rows.add(
                    SideBySideRowData(
                        null, line.newLineNum,
                        null, line.content,
                        SideBySideType.ADDED
                    )
                )
            }
        }
        i++
    }
    return rows
}

@Composable
private fun SideBySideRow(row: SideBySideRowData, language: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        SideBySidePane(
            lineNum = row.oldLineNum,
            content = row.oldContent,
            backgroundColor = when (row.type) {
                SideBySideType.REMOVED, SideBySideType.MODIFIED -> removedLineBg
                else -> Color.Transparent
            },
            language = language,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        SideBySidePane(
            lineNum = row.newLineNum,
            content = row.newContent,
            backgroundColor = when (row.type) {
                SideBySideType.ADDED, SideBySideType.MODIFIED -> addedLineBg
                else -> Color.Transparent
            },
            language = language,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SideBySidePane(
    lineNum: Int?,
    content: String?,
    backgroundColor: Color,
    language: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(backgroundColor)
            .height(IntrinsicSize.Min)
    ) {
        LineNumberCell(
            number = lineNum,
            modifier = Modifier.width(40.dp)
        )
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight()
                .padding(start = 2.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (content != null) {
                Text(
                    text = if (lineNum == null) "+" else if (content != null && lineNum != null) " " else "-",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = indicatorFg
                )
            }
        }
        if (content != null) {
            BasicText(
                text = highlightLine(content, language),
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                    .weight(1f),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
