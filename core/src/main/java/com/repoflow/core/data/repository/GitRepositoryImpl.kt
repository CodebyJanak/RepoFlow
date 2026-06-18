package com.repoflow.core.data.repository

import com.repoflow.core.data.git.GitManager
import com.repoflow.core.data.local.dao.RepositoryDao
import com.repoflow.core.data.mapper.RepositoryMapper.toDomain
import com.repoflow.core.data.mapper.RepositoryMapper.toEntity
import com.repoflow.core.data.remote.ApiService
import com.repoflow.core.domain.model.Branch
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.model.Contributor
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.domain.model.Release
import com.repoflow.core.domain.model.StatusFile
import com.repoflow.core.domain.model.Tag
import com.repoflow.core.domain.repository.GitRepository as GitRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val repositoryDao: RepositoryDao,
    private val gitManager: GitManager
) : GitRepositoryInterface {

    override suspend fun getRepositories(): Result<List<GitRepository>> {
        return try {
            val response = apiService.getRepositories()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val repos = dtos.map { it.toDomain() }
                val entities = repos.map { it.toEntity() }
                repositoryDao.insertAll(entities)
                Result.success(repos)
            } else {
                Result.failure(Exception("Failed to fetch repos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchRepositories(query: String): Result<List<GitRepository>> {
        return try {
            val response = apiService.searchRepositories(query)
            if (response.isSuccessful) {
                val dtos = response.body()?.items ?: emptyList()
                val repos = dtos.map { it.toDomain() }
                Result.success(repos)
            } else {
                Result.failure(Exception("Search failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLocalRepositoriesFlow(): Flow<List<GitRepository>> {
        return repositoryDao.getAllRepositories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getLocalRepositoriesFlow(sortOrder: SortOrder): Flow<List<GitRepository>> {
        val flow = when (sortOrder) {
            SortOrder.STARS -> repositoryDao.getAllRepositoriesSortedByStars()
            SortOrder.NAME -> repositoryDao.getAllRepositoriesSortedByName()
            SortOrder.FORKS -> repositoryDao.getAllRepositoriesSortedByForks()
            SortOrder.UPDATED -> repositoryDao.getAllRepositories()
        }
        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    fun searchLocalRepositories(query: String): Flow<List<GitRepository>> {
        return repositoryDao.searchRepositories(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getFavoriteRepositoriesFlow(): Flow<List<GitRepository>> {
        return repositoryDao.getFavoriteRepositories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(repoId: Long, isFavorite: Boolean) {
        repositoryDao.setFavorite(repoId, isFavorite)
    }

    override suspend fun getRepository(owner: String, name: String): Result<GitRepository> {
        return try {
            val response = apiService.getRepository(owner, name)
            if (response.isSuccessful) {
                val dto = response.body() ?: return Result.failure(Exception("Empty response"))
                Result.success(dto.toDomain())
            } else {
                Result.failure(Exception("Failed to fetch repository: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cloneRepository(
        url: String,
        localPath: String,
        onProgress: ((String, Int) -> Unit)?
    ): Result<Unit> = gitManager.cloneRepository(url, localPath, onProgress)

    override suspend fun fetchRepository(
        localPath: String,
        onProgress: ((String, Int) -> Unit)?
    ): Result<Unit> = gitManager.fetchRepository(localPath, onProgress = onProgress)

    override suspend fun pullRepository(
        localPath: String,
        rebase: Boolean,
        onProgress: ((String, Int) -> Unit)?
    ): Result<Unit> = gitManager.pullRepository(localPath, rebase = rebase, onProgress = onProgress).map {}

    override suspend fun pushRepository(
        localPath: String,
        force: Boolean,
        onProgress: ((String, Int) -> Unit)?
    ): Result<Unit> = gitManager.pushRepository(localPath, force = force, onProgress = onProgress).map {}

    override suspend fun commit(
        localPath: String,
        message: String,
        authorName: String,
        authorEmail: String
    ): Result<Commit> {
        return gitManager.stageAll(localPath).mapCatching {
            gitManager.commit(localPath, message, authorName, authorEmail).getOrThrow()
        }
    }

    override suspend fun getStatus(localPath: String): Result<List<StatusFile>> =
        gitManager.getStatus(localPath)

    override suspend fun stageFile(localPath: String, filePath: String): Result<Unit> =
        gitManager.stageFile(localPath, filePath)

    override suspend fun unstageFile(localPath: String, filePath: String): Result<Unit> =
        gitManager.unstageFile(localPath, filePath)

    override suspend fun stageAll(localPath: String): Result<Unit> =
        gitManager.stageAll(localPath)

    override suspend fun unstageAll(localPath: String): Result<Unit> =
        gitManager.unstageAll(localPath)

    override suspend fun getBranches(localPath: String): Result<List<String>> =
        gitManager.getLocalBranches(localPath)

    override suspend fun createBranch(localPath: String, branchName: String): Result<Unit> =
        gitManager.createBranch(localPath, branchName)

    override suspend fun deleteBranch(localPath: String, branchName: String): Result<Unit> =
        gitManager.deleteBranch(localPath, branchName)

    override suspend fun renameBranch(
        localPath: String,
        oldName: String,
        newName: String
    ): Result<Unit> = gitManager.renameBranch(localPath, oldName, newName)

    override suspend fun checkoutBranch(localPath: String, branchName: String): Result<Unit> =
        gitManager.checkoutBranch(localPath, branchName)

    override suspend fun mergeBranch(
        localPath: String,
        sourceBranch: String,
        targetBranch: String
    ): Result<Unit> {
        return gitManager.mergeBranch(localPath, sourceBranch, targetBranch).mapCatching {
            if (!it.isSuccessful) {
                val conflictMsg = if (it.conflicts.isNotEmpty()) {
                    " Conflicts: ${it.conflicts.joinToString(", ")}"
                } else ""
                throw Exception("Merge failed: ${it.status}$conflictMsg")
            }
        }
    }

    override suspend fun getCommitHistory(localPath: String, maxCount: Int): Result<List<Commit>> =
        gitManager.getCommitHistory(localPath, maxCount)

    override suspend fun getTags(localPath: String): Result<List<Tag>> =
        gitManager.getTags(localPath)

    override suspend fun createTag(
        localPath: String,
        tagName: String,
        message: String?
    ): Result<Unit> = gitManager.createTag(localPath, tagName, message)

    override suspend fun deleteTag(localPath: String, tagName: String): Result<Unit> =
        gitManager.deleteTag(localPath, tagName)

    override suspend fun getRemoteBranches(owner: String, name: String): Result<List<Branch>> {
        return try {
            val response = apiService.getBranches(owner, name)
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                Result.success(dtos.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch branches: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRemoteCommits(
        owner: String,
        name: String,
        branch: String?
    ): Result<List<Commit>> {
        return try {
            val response = apiService.getCommits(owner, name, sha = branch)
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                Result.success(dtos.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch commits: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContributors(owner: String, name: String): Result<List<Contributor>> {
        return try {
            val response = apiService.getContributors(owner, name)
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                Result.success(dtos.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch contributors: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReleases(owner: String, name: String): Result<List<Release>> {
        return try {
            val response = apiService.getReleases(owner, name)
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                Result.success(dtos.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch releases: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

enum class SortOrder {
    STARS, NAME, FORKS, UPDATED
}
