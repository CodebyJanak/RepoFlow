package com.repoflow.core.data.repository

import com.repoflow.core.data.local.dao.RepositoryDao
import com.repoflow.core.data.mapper.RepositoryMapper.toDomain
import com.repoflow.core.data.mapper.RepositoryMapper.toEntity
import com.repoflow.core.data.remote.ApiService
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.domain.repository.GitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val repositoryDao: RepositoryDao
) : GitRepository {

    override suspend fun getRepositories(): Result<List<GitRepository>> {
        return try {
            val response = apiService.getRepositories()
            if (response.isSuccessful) {
                val repos = response.body()?.map { it.toDomain() } ?: emptyList()
                repositoryDao.insertAll(repos.map { it.toEntity() })
                Result.success(repos)
            } else {
                Result.failure(Exception("Failed to fetch repos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRepository(owner: String, name: String): Result<GitRepository> {
        return Result.failure(NotImplementedError("Single repo fetch not yet implemented"))
    }

    override suspend fun cloneRepository(url: String, localPath: String): Result<Unit> {
        return Result.failure(NotImplementedError("Clone not yet implemented"))
    }

    override suspend fun pullRepository(localPath: String): Result<Unit> {
        return Result.failure(NotImplementedError("Pull not yet implemented"))
    }

    override suspend fun pushRepository(localPath: String, force: Boolean): Result<Unit> {
        return Result.failure(NotImplementedError("Push not yet implemented"))
    }

    override suspend fun fetchRepository(localPath: String): Result<Unit> {
        return Result.failure(NotImplementedError("Fetch not yet implemented"))
    }

    override suspend fun getBranches(localPath: String): Result<List<String>> {
        return Result.failure(NotImplementedError("Branch listing not yet implemented"))
    }

    override suspend fun createBranch(localPath: String, branchName: String): Result<Unit> {
        return Result.failure(NotImplementedError("Branch creation not yet implemented"))
    }

    override suspend fun deleteBranch(localPath: String, branchName: String): Result<Unit> {
        return Result.failure(NotImplementedError("Branch deletion not yet implemented"))
    }

    override suspend fun renameBranch(localPath: String, oldName: String, newName: String): Result<Unit> {
        return Result.failure(NotImplementedError("Branch rename not yet implemented"))
    }

    override suspend fun checkoutBranch(localPath: String, branchName: String): Result<Unit> {
        return Result.failure(NotImplementedError("Branch checkout not yet implemented"))
    }

    override suspend fun mergeBranch(
        localPath: String,
        sourceBranch: String,
        targetBranch: String
    ): Result<Unit> {
        return Result.failure(NotImplementedError("Branch merge not yet implemented"))
    }

    override suspend fun getCommitHistory(localPath: String): Result<List<Commit>> {
        return Result.failure(NotImplementedError("Commit history not yet implemented"))
    }
}
