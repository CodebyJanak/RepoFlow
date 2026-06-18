package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.Branch
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.model.Contributor
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.domain.model.Release
import com.repoflow.core.domain.model.StatusFile
import com.repoflow.core.domain.model.Tag

interface GitRepository {
    suspend fun getRepositories(): Result<List<GitRepository>>
    suspend fun searchRepositories(query: String): Result<List<GitRepository>>
    suspend fun getRepository(owner: String, name: String): Result<GitRepository>
    suspend fun toggleFavorite(repoId: Long, isFavorite: Boolean)

    suspend fun cloneRepository(url: String, localPath: String, onProgress: ((String, Int) -> Unit)? = null): Result<Unit>
    suspend fun fetchRepository(localPath: String, onProgress: ((String, Int) -> Unit)? = null): Result<Unit>
    suspend fun pullRepository(localPath: String, rebase: Boolean = false, onProgress: ((String, Int) -> Unit)? = null): Result<Unit>
    suspend fun pushRepository(localPath: String, force: Boolean = false, onProgress: ((String, Int) -> Unit)? = null): Result<Unit>

    suspend fun commit(localPath: String, message: String, authorName: String, authorEmail: String): Result<Commit>

    suspend fun getBranches(localPath: String): Result<List<String>>
    suspend fun createBranch(localPath: String, branchName: String): Result<Unit>
    suspend fun deleteBranch(localPath: String, branchName: String): Result<Unit>
    suspend fun renameBranch(localPath: String, oldName: String, newName: String): Result<Unit>
    suspend fun checkoutBranch(localPath: String, branchName: String): Result<Unit>
    suspend fun mergeBranch(localPath: String, sourceBranch: String, targetBranch: String): Result<Unit>
    suspend fun getCommitHistory(localPath: String, maxCount: Int = 50): Result<List<Commit>>

    suspend fun getStatus(localPath: String): Result<List<StatusFile>>
    suspend fun stageFile(localPath: String, filePath: String): Result<Unit>
    suspend fun unstageFile(localPath: String, filePath: String): Result<Unit>
    suspend fun stageAll(localPath: String): Result<Unit>
    suspend fun unstageAll(localPath: String): Result<Unit>

    suspend fun getTags(localPath: String): Result<List<Tag>>
    suspend fun createTag(localPath: String, tagName: String, message: String? = null): Result<Unit>
    suspend fun deleteTag(localPath: String, tagName: String): Result<Unit>

    suspend fun getRemoteBranches(owner: String, name: String): Result<List<Branch>>
    suspend fun getRemoteCommits(owner: String, name: String, branch: String? = null): Result<List<Commit>>
    suspend fun getContributors(owner: String, name: String): Result<List<Contributor>>
    suspend fun getReleases(owner: String, name: String): Result<List<Release>>
}
