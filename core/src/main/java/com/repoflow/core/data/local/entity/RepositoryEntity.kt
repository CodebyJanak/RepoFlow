package com.repoflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repositories")
data class RepositoryEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val isPrivate: Boolean,
    val stars: Int,
    val forks: Int,
    val defaultBranch: String,
    val localPath: String?,
    val lastSyncedAt: Long?
)
