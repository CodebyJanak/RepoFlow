package com.repoflow.core.domain.usecase.git

import com.repoflow.core.data.git.GitException
import com.repoflow.core.domain.repository.GitRepository
import javax.inject.Inject

class BranchUseCase @Inject constructor(
    private val gitRepository: GitRepository
) {
    suspend fun list(localPath: String): Result<List<String>> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        return gitRepository.getBranches(localPath)
    }

    suspend fun create(localPath: String, branchName: String): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (branchName.isBlank()) return Result.failure(GitException("Branch name cannot be empty"))
        return gitRepository.createBranch(localPath, branchName)
    }

    suspend fun delete(localPath: String, branchName: String): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (branchName.isBlank()) return Result.failure(GitException("Branch name cannot be empty"))
        return gitRepository.deleteBranch(localPath, branchName)
    }

    suspend fun rename(localPath: String, oldName: String, newName: String): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (oldName.isBlank()) return Result.failure(GitException("Current branch name cannot be empty"))
        if (newName.isBlank()) return Result.failure(GitException("New branch name cannot be empty"))
        return gitRepository.renameBranch(localPath, oldName, newName)
    }

    suspend fun checkout(localPath: String, branchName: String): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (branchName.isBlank()) return Result.failure(GitException("Branch name cannot be empty"))
        return gitRepository.checkoutBranch(localPath, branchName)
    }

    suspend fun checkoutNewBranch(localPath: String, branchName: String): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (branchName.isBlank()) return Result.failure(GitException("Branch name cannot be empty"))
        val checkoutResult = gitRepository.checkoutBranch(localPath, branchName)
        if (checkoutResult.isSuccess) return checkoutResult
        return runCatching {
            gitRepository.createBranch(localPath, branchName).getOrThrow()
            gitRepository.checkoutBranch(localPath, branchName).getOrThrow()
        }
    }

    suspend fun merge(localPath: String, sourceBranch: String, targetBranch: String): Result<Unit> {
        if (localPath.isBlank()) return Result.failure(GitException("Local path cannot be empty"))
        if (sourceBranch.isBlank()) return Result.failure(GitException("Source branch name cannot be empty"))
        if (targetBranch.isBlank()) return Result.failure(GitException("Target branch name cannot be empty"))
        return gitRepository.mergeBranch(localPath, sourceBranch, targetBranch)
    }
}
