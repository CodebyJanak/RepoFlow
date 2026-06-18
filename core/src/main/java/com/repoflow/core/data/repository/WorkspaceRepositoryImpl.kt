package com.repoflow.core.data.repository

import com.repoflow.core.domain.model.WorkspaceFile
import com.repoflow.core.domain.repository.WorkspaceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceRepositoryImpl @Inject constructor() : WorkspaceRepository {

    override suspend fun getWorkspaceFiles(path: String): Result<List<WorkspaceFile>> {
        return Result.failure(NotImplementedError("Workspace file listing not yet implemented"))
    }

    override suspend fun getFileContent(path: String): Result<String> {
        return Result.failure(NotImplementedError("File content reading not yet implemented"))
    }

    override suspend fun saveFileContent(path: String, content: String): Result<Unit> {
        return Result.failure(NotImplementedError("File saving not yet implemented"))
    }

    override suspend fun createDirectory(path: String): Result<Unit> {
        return Result.failure(NotImplementedError("Directory creation not yet implemented"))
    }

    override suspend fun deleteFile(path: String): Result<Unit> {
        return Result.failure(NotImplementedError("File deletion not yet implemented"))
    }

    override suspend fun deleteDirectory(path: String): Result<Unit> {
        return Result.failure(NotImplementedError("Directory deletion not yet implemented"))
    }

    override suspend fun searchFiles(query: String, path: String): Result<List<WorkspaceFile>> {
        return Result.failure(NotImplementedError("File search not yet implemented"))
    }

    override fun getRootWorkspacePath(): String {
        return ""
    }
}
