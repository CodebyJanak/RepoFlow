package com.repoflow.core.domain.model

data class Artifact(
    val id: Long,
    val name: String,
    val sizeInBytes: Long,
    val archiveDownloadUrl: String,
    val expired: Boolean,
    val createdAt: String,
    val expiresAt: String?
) {
    val sizeFormatted: String get() = when {
        sizeInBytes < 1024 -> "$sizeInBytes B"
        sizeInBytes < 1024 * 1024 -> String.format("%.1f KB", sizeInBytes / 1024.0)
        else -> String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0))
    }
}
