package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.WorkspaceFile

interface WorkspaceRepository {
    suspend fun getWorkspaceFiles(path: String): Result<List<WorkspaceFile>>
    suspend fun getFileContent(path: String): Result<String>
    suspend fun saveFileContent(path: String, content: String): Result<Unit>
    suspend fun createDirectory(path: String): Result<Unit>
    suspend fun deleteFile(path: String): Result<Unit>
    suspend fun deleteDirectory(path: String): Result<Unit>
    suspend fun searchFiles(query: String, path: String): Result<List<WorkspaceFile>>
    fun getRootWorkspacePath(): String
}
