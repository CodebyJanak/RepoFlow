package com.repoflow.core.domain.usecase.git

import com.repoflow.core.data.git.GitException
import com.repoflow.core.domain.model.Tag
import com.repoflow.core.domain.repository.GitRepository
import javax.inject.Inject

class TagUseCase @Inject constructor(
    private val gitRepository: GitRepository
) {
    suspend fun list(localPath: String): Result<List<Tag>> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        return gitRepository.getTags(localPath)
    }

    suspend fun create(localPath: String, tagName: String, message: String? = null): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (tagName.isBlank()) return Result.failure(GitException("Tag name cannot be empty"))
        return gitRepository.createTag(localPath, tagName, message)
    }

    suspend fun delete(localPath: String, tagName: String): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (tagName.isBlank()) return Result.failure(GitException("Tag name cannot be empty"))
        return gitRepository.deleteTag(localPath, tagName)
    }
}
