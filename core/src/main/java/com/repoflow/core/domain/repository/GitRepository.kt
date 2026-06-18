package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.model.GitRepository

interface GitRepository {
    suspend fun getRepositories(): Result<List<GitRepository>>
    suspend fun searchRepositories(query: String): Result<List<GitRepository>>
    suspend fun getRepository(owner: String, name: String): Result<GitRepository>
    suspend fun toggleFavorite(repoId: Long, isFavorite: Boolean)
    suspend fun cloneRepository(url: String, localPath: String): Result<Unit>
    suspend fun pullRepository(localPath: String): Result<Unit>
    suspend fun pushRepository(localPath: String, force: Boolean = false): Result<Unit>
    suspend fun fetchRepository(localPath: String): Result<Unit>
    suspend fun getBranches(localPath: String): Result<List<String>>
    suspend fun createBranch(localPath: String, branchName: String): Result<Unit>
    suspend fun deleteBranch(localPath: String, branchName: String): Result<Unit>
    suspend fun renameBranch(localPath: String, oldName: String, newName: String): Result<Unit>
    suspend fun checkoutBranch(localPath: String, branchName: String): Result<Unit>
    suspend fun mergeBranch(localPath: String, sourceBranch: String, targetBranch: String): Result<Unit>
    suspend fun getCommitHistory(localPath: String): Result<List<Commit>>
}
