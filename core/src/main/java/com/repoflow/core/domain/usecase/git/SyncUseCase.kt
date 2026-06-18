package com.repoflow.core.domain.usecase.git

import com.repoflow.core.data.git.GitException
import com.repoflow.core.domain.repository.GitRepository
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val gitRepository: GitRepository
) {
    suspend fun fetch(
        localPath: String,
        onProgress: ((String, Int) -> Unit)? = null
    ): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        return gitRepository.fetchRepository(localPath, onProgress)
    }

    suspend fun pull(
        localPath: String,
        rebase: Boolean = false,
        onProgress: ((String, Int) -> Unit)? = null
    ): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        return gitRepository.pullRepository(localPath, rebase, onProgress)
    }

    suspend fun push(
        localPath: String,
        force: Boolean = false,
        onProgress: ((String, Int) -> Unit)? = null
    ): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        return gitRepository.pushRepository(localPath, force, onProgress)
    }

    suspend fun sync(
        localPath: String,
        onProgress: ((String, Int) -> Unit)? = null
    ): Result<Unit> {
        val fetchResult = fetch(localPath, onProgress)
        if (fetchResult.isFailure) return fetchResult
        val pullResult = pull(localPath, onProgress = onProgress)
        if (pullResult.isFailure) return pullResult
        return push(localPath, onProgress = onProgress)
    }
}
