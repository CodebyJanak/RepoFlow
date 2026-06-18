package com.repoflow.core.domain.model

enum class DiffLineType {
    ADDED,
    REMOVED,
    UNCHANGED
}

data class DiffLine(
    val type: DiffLineType,
    val oldLineNum: Int?,
    val newLineNum: Int?,
    val content: String
)

data class DiffHunk(
    val oldStart: Int,
    val oldCount: Int,
    val newStart: Int,
    val newCount: Int,
    val lines: List<DiffLine>
)

data class DiffFile(
    val oldPath: String,
    val newPath: String,
    val hunks: List<DiffHunk>
) {
    val isEmpty: Boolean get() = hunks.isEmpty() || hunks.all { it.lines.isEmpty() }

    val addedLines: Int get() = hunks.sumOf { h -> h.lines.count { it.type == DiffLineType.ADDED } }
    val removedLines: Int get() = hunks.sumOf { h -> h.lines.count { it.type == DiffLineType.REMOVED } }
}
