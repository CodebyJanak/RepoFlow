package com.repoflow.core.domain.usecase.git

import com.repoflow.core.data.git.GitException
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.repository.GitRepository
import javax.inject.Inject

class HistoryUseCase @Inject constructor(
    private val gitRepository: GitRepository
) {
    suspend operator fun invoke(
        localPath: String,
        maxCount: Int = 50
    ): Result<List<Commit>> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (maxCount <= 0) return Result.failure(GitException("Max count must be positive"))
        return gitRepository.getCommitHistory(localPath, maxCount)
    }
}
