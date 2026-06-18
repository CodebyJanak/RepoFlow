package com.repoflow.core.domain.usecase.git

import com.repoflow.core.data.git.GitException
import com.repoflow.core.domain.repository.GitRepository
import javax.inject.Inject

class CloneUseCase @Inject constructor(
    private val gitRepository: GitRepository
) {
    suspend operator fun invoke(
        url: String,
        localPath: String,
        onProgress: ((String, Int) -> Unit)? = null
    ): Result<Unit> {
        if (url.isBlank()) return Result.failure(GitException("Clone URL cannot be empty"))
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        return gitRepository.cloneRepository(url, localPath, onProgress)
    }
}
