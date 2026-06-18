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
    val language: String?,
    val openIssues: Int,
    val defaultBranch: String,
    val ownerLogin: String,
    val ownerAvatarUrl: String,
    val isFavorite: Boolean = false,
    val isCloned: Boolean = false,
    val localPath: String? = null,
    val lastSyncedAt: Long? = null
)
