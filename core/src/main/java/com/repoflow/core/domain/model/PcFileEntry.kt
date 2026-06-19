package com.repoflow.core.domain.model

data class PcFileEntry(
    val name: String,
    val type: PcFileType,
    val path: String,
    val size: Long?,
    val modifiedAt: String?
)

enum class PcFileType {
    FILE,
    DIRECTORY;

    companion object {
        fun fromString(value: String): PcFileType = when (value.lowercase()) {
            "file" -> FILE
            "directory" -> DIRECTORY
            else -> FILE
        }
    }
}
