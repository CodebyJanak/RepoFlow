package com.repoflow.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.repoflow.core.domain.model.WorkspaceFile
import com.repoflow.core.domain.repository.WorkspaceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WorkspaceRepository {

    private val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
    private val contentResolver = context.contentResolver

    override fun getRootWorkspaceUri(): String? {
        return prefs.getString("root_uri", null)
    }

    override fun setRootWorkspaceUri(uri: String) {
        prefs.edit().putString("root_uri", uri).apply()
    }

    override suspend fun getWorkspaceFiles(uri: Uri): Result<List<WorkspaceFile>> {
        return runCatching {
            val treeUri = toTreeUri(uri)
            val dir = DocumentFile.fromTreeUri(context, treeUri)
                ?: return Result.failure(Exception("Invalid directory URI"))
            dir.listFiles()
                .map { it.toWorkspaceFile() }
                .sortedWith(compareByDescending<WorkspaceFile> { it.isDirectory }.thenBy { it.name.lowercase() })
        }
    }

    override suspend fun getFileContent(uri: Uri): Result<String> {
        return runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("Unable to read file")
        }
    }

    override suspend fun saveFileContent(uri: Uri, content: String): Result<Unit> {
        return runCatching {
            contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(content) }
                ?: throw Exception("Unable to write file")
        }
    }

    override suspend fun createDirectory(parentUri: Uri, name: String): Result<Uri> {
        return runCatching {
            val treeUri = toTreeUri(parentUri)
            val parent = DocumentFile.fromTreeUri(context, treeUri)
                ?: throw Exception("Invalid parent directory URI")
            val dir = parent.createDirectory(name)
                ?: throw Exception("Failed to create directory")
            dir.uri
        }
    }

    override suspend fun deleteFile(uri: Uri): Result<Unit> {
        return runCatching {
            val file = DocumentFile.fromSingleUri(context, uri)
                ?: throw Exception("Invalid file URI")
            if (!file.delete()) throw Exception("Failed to delete file")
        }
    }

    override suspend fun deleteDirectory(uri: Uri): Result<Unit> {
        return deleteFile(uri)
    }

    override suspend fun searchFiles(query: String, uri: Uri): Result<List<WorkspaceFile>> {
        return runCatching {
            val results = mutableListOf<WorkspaceFile>()
            searchRecursive(uri, query, results)
            results
        }
    }

    override suspend fun isRepository(uri: Uri): Result<Boolean> {
        return runCatching {
            val treeUri = toTreeUri(uri)
            val dir = DocumentFile.fromTreeUri(context, treeUri)
                ?: return Result.failure(Exception("Invalid directory URI"))
            dir.listFiles().any { it.name == ".git" && it.isDirectory }
        }
    }

    private fun searchRecursive(uri: Uri, query: String, results: MutableList<WorkspaceFile>) {
        val treeUri = toTreeUri(uri)
        val dir = DocumentFile.fromTreeUri(context, treeUri) ?: return
        for (file in dir.listFiles()) {
            if (file.name?.contains(query, ignoreCase = true) == true) {
                results.add(file.toWorkspaceFile())
            }
            if (file.isDirectory) {
                searchRecursive(file.uri, query, results)
            }
        }
    }

    private fun toTreeUri(uri: Uri): Uri {
        val uriStr = uri.toString()
        return if (uriStr.contains("/document/")) {
            Uri.parse(uriStr.replace("/document/", "/tree/"))
        } else {
            uri
        }
    }

    private fun DocumentFile.toWorkspaceFile(): WorkspaceFile {
        return WorkspaceFile(
            name = name ?: "Unknown",
            uri = uri.toString(),
            isDirectory = isDirectory,
            size = length(),
            lastModified = lastModified()
        )
    }
}
