package com.repoflow.core.domain.repository

import android.net.Uri
import com.repoflow.core.domain.model.WorkspaceFile

interface WorkspaceRepository {
    suspend fun getWorkspaceFiles(uri: Uri): Result<List<WorkspaceFile>>
    suspend fun getFileContent(uri: Uri): Result<String>
    suspend fun saveFileContent(uri: Uri, content: String): Result<Unit>
    suspend fun createDirectory(parentUri: Uri, name: String): Result<Uri>
    suspend fun deleteFile(uri: Uri): Result<Unit>
    suspend fun deleteDirectory(uri: Uri): Result<Unit>
    suspend fun searchFiles(query: String, uri: Uri): Result<List<WorkspaceFile>>
    suspend fun isRepository(uri: Uri): Result<Boolean>
    fun getRootWorkspaceUri(): String?
    fun setRootWorkspaceUri(uri: String)
}
