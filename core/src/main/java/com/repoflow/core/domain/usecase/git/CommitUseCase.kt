package com.repoflow.core.domain.usecase.git

import com.repoflow.core.data.git.GitException
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.repository.GitRepository
import javax.inject.Inject

class CommitUseCase @Inject constructor(
    private val gitRepository: GitRepository
) {
    suspend operator fun invoke(
        localPath: String,
        message: String,
        authorName: String,
        authorEmail: String
    ): Result<Commit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (message.isBlank()) return Result.failure(GitException("Commit message cannot be empty"))
        if (authorName.isBlank()) return Result.failure(GitException("Author name cannot be empty"))
        if (authorEmail.isBlank()) return Result.failure(GitException("Author email cannot be empty"))
        return gitRepository.commit(localPath, message, authorName, authorEmail)
    }

    suspend fun commitAndPush(
        localPath: String,
        message: String,
        authorName: String,
        authorEmail: String,
        force: Boolean = false,
        onProgress: ((String, Int) -> Unit)? = null
    ): Result<Commit> {
        val commitResult = invoke(localPath, message, authorName, authorEmail)
        if (commitResult.isFailure) return commitResult
        val pushResult = gitRepository.pushRepository(localPath, force, onProgress)
        if (pushResult.isFailure) return Result.failure(pushResult.exceptionOrNull() ?: GitException("Push failed"))
        return commitResult
    }
}
