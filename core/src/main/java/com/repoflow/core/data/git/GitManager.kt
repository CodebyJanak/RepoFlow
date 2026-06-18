package com.repoflow.core.data.git

import com.repoflow.core.data.local.datastore.SecureStorage
import com.repoflow.core.domain.model.FileStatusType
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.model.StatusFile
import com.repoflow.core.domain.model.Tag
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.errors.CanceledException
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.api.errors.RefNotFoundException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.api.errors.WrongRepositoryStateException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class GitManager @Inject constructor(
    private val secureStorage: SecureStorage
) {
    private fun credentialsProvider() =
        UsernamePasswordCredentialsProvider(secureStorage.getAccessTokenSync() ?: "", "")

    suspend fun cloneRepository(
        url: String,
        localPath: String,
        onProgress: ((message: String, progress: Int) -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Clone") {
            val monitor = SimpleProgressMonitor(onProgress)
            Git.cloneRepository()
                .setURI(url)
                .setDirectory(File(localPath))
                .setCredentialsProvider(credentialsProvider())
                .setProgressMonitor(monitor)
                .call()
                .use { _ -> }
        }
    }

    suspend fun fetchRepository(
        localPath: String,
        remote: String = Constants.DEFAULT_REMOTE_NAME,
        onProgress: ((message: String, progress: Int) -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Fetch") {
            openGit(localPath).use { git ->
                val monitor = SimpleProgressMonitor(onProgress)
                git.fetch()
                    .setRemote(remote)
                    .setCredentialsProvider(credentialsProvider())
                    .setProgressMonitor(monitor)
                    .call()
            }
        }
    }

    suspend fun pullRepository(
        localPath: String,
        remote: String = Constants.DEFAULT_REMOTE_NAME,
        branch: String? = null,
        rebase: Boolean = false,
        onProgress: ((message: String, progress: Int) -> Unit)? = null
    ): Result<PullResultInfo> = withContext(Dispatchers.IO) {
        runCatchingJGit("Pull") {
            openGit(localPath).use { git ->
                val monitor = SimpleProgressMonitor(onProgress)
                val result = git.pull()
                    .setRemote(remote)
                    .setRebase(rebase)
                    .apply { branch?.let { setRemoteBranchName(it) } }
                    .setCredentialsProvider(credentialsProvider())
                    .setProgressMonitor(monitor)
                    .call()
                PullResultInfo(
                    isSuccessful = result.isSuccessful,
                    fetchResult = result.fetchResult?.messages ?: "",
                    mergeResult = result.mergeResult?.toString() ?: "",
                    rebaseResult = result.rebaseResult?.status?.name() ?: ""
                )
            }
        }
    }

    suspend fun pushRepository(
        localPath: String,
        remote: String = Constants.DEFAULT_REMOTE_NAME,
        force: Boolean = false,
        onProgress: ((message: String, progress: Int) -> Unit)? = null
    ): Result<PushResultInfo> = withContext(Dispatchers.IO) {
        runCatchingJGit("Push") {
            openGit(localPath).use { git ->
                val monitor = SimpleProgressMonitor(onProgress)
                val results = git.push()
                    .setRemote(remote)
                    .setForce(force)
                    .setCredentialsProvider(credentialsProvider())
                    .setProgressMonitor(monitor)
                    .call()
                val result = results.firstOrNull()
                PushResultInfo(
                    isSuccessful = result?.remoteUpdates?.all {
                        it.status == RemoteRefUpdate.Status.OK ||
                        it.status == RemoteRefUpdate.Status.UP_TO_DATE
                    } ?: false,
                    messages = result?.messages ?: "",
                    updates = result?.remoteUpdates?.map {
                        "${it.remoteName}: ${it.status.name()}"
                    } ?: emptyList()
                )
            }
        }
    }

    suspend fun getStatus(localPath: String): Result<List<StatusFile>> = withContext(Dispatchers.IO) {
        runCatchingJGit("Status") {
            openGit(localPath).use { git ->
                val status = git.status().call()
                val files = mutableListOf<StatusFile>()

                status.modified.forEach { path ->
                    files.add(StatusFile(path, FileStatusType.MODIFIED, staged = false))
                }
                status.changed.forEach { path ->
                    files.add(StatusFile(path, FileStatusType.MODIFIED, staged = true))
                }
                status.untracked.forEach { path ->
                    files.add(StatusFile(path, FileStatusType.ADDED, staged = false))
                }
                status.added.forEach { path ->
                    files.add(StatusFile(path, FileStatusType.ADDED, staged = true))
                }
                status.missing.forEach { path ->
                    files.add(StatusFile(path, FileStatusType.DELETED, staged = false))
                }
                status.removed.forEach { path ->
                    files.add(StatusFile(path, FileStatusType.DELETED, staged = true))
                }
                status.conflicting.forEach { path ->
                    files.add(StatusFile(path, FileStatusType.CONFLICTING, staged = false))
                }

                files.sortedBy { it.path }
            }
        }
    }

    suspend fun stageFile(localPath: String, filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Stage") {
            openGit(localPath).use { git ->
                git.add().addFilepattern(filePath).call()
            }
        }
    }

    suspend fun stageAll(localPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Stage all") {
            openGit(localPath).use { git ->
                git.add().addFilepattern(".").call()
            }
        }
    }

    suspend fun unstageFile(localPath: String, filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Unstage") {
            openGit(localPath).use { git ->
                git.reset().addPath(filePath).call()
            }
        }
    }

    suspend fun unstageAll(localPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Unstage all") {
            openGit(localPath).use { git ->
                git.reset().call()
            }
        }
    }

    suspend fun commit(
        localPath: String,
        message: String,
        authorName: String,
        authorEmail: String
    ): Result<Commit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Commit") {
            openGit(localPath).use { git ->
                val revCommit = git.commit()
                    .setMessage(message)
                    .setAuthor(authorName, authorEmail)
                    .setCommitter(authorName, authorEmail)
                    .call()
                Commit(
                    hash = revCommit.name,
                    message = revCommit.fullMessage.trim(),
                    author = revCommit.authorIdent.name,
                    authorLogin = null,
                    authorAvatarUrl = null,
                    timestamp = revCommit.commitTime.toLong() * 1000
                )
            }
        }
    }

    suspend fun getLocalBranches(localPath: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatchingJGit("List branches") {
            openGit(localPath).use { git ->
                git.branchList()
                    .setListMode(ListBranchCommand.ListOption.ALL)
                    .call()
                    .map { ref ->
                        ref.name.removePrefix("refs/heads/")
                            .removePrefix("refs/remotes/")
                    }
            }
        }
    }

    suspend fun createBranch(localPath: String, branchName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Create branch") {
            openGit(localPath).use { git ->
                git.branchCreate().setName(branchName).call()
            }
        }
    }

    suspend fun deleteBranch(
        localPath: String,
        branchName: String,
        force: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Delete branch") {
            openGit(localPath).use { git ->
                git.branchDelete()
                    .setBranchNames(Constants.R_HEADS + branchName)
                    .setForce(force)
                    .call()
            }
        }
    }

    suspend fun renameBranch(localPath: String, oldName: String, newName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatchingJGit("Rename branch") {
                openGit(localPath).use { git ->
                    git.branchRename()
                        .setOldName(oldName)
                        .setNewName(newName)
                        .call()
                }
            }
        }

    suspend fun checkoutBranch(
        localPath: String,
        branchName: String,
        create: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Checkout") {
            openGit(localPath).use { git ->
                git.checkout()
                    .setName(branchName)
                    .setCreateBranch(create)
                    .call()
            }
        }
    }

    suspend fun mergeBranch(
        localPath: String,
        sourceBranch: String,
        targetBranch: String
    ): Result<MergeResultInfo> = withContext(Dispatchers.IO) {
        runCatchingJGit("Merge") {
            openGit(localPath).use { git ->
                git.checkout().setName(targetBranch).call()
                val sourceRef = git.repository.resolve(sourceBranch)
                    ?: throw RefNotFoundException("Source branch not found: $sourceBranch")
                val mergeResult = git.merge().include(sourceRef).call()
                MergeResultInfo(
                    isSuccessful = mergeResult.mergeStatus.isSuccessful,
                    status = mergeResult.mergeStatus.name(),
                    newHead = mergeResult.newHead?.name ?: "",
                    conflicts = mergeResult.conflicts?.keys?.toList() ?: emptyList()
                )
            }
        }
    }

    suspend fun getTags(localPath: String): Result<List<Tag>> = withContext(Dispatchers.IO) {
        runCatchingJGit("List tags") {
            openGit(localPath).use { git ->
                val tagRefs = git.tagList().call()
                val repo = git.repository
                RevWalk(repo).use { revWalk ->
                    tagRefs.map { ref ->
                        val objId = ref.objectId
                        val commitId = if (ref.peeledObjectId != null) ref.peeledObjectId else objId
                        val tagName = ref.name.removePrefix(Constants.R_TAGS)
                        val message = try {
                            revWalk.parseTag(objId)?.fullMessage?.trim()
                        } catch (_: Exception) { null }
                        Tag(name = tagName, commitHash = commitId.name, message = message)
                    }
                }
            }
        }
    }

    suspend fun createTag(
        localPath: String,
        tagName: String,
        message: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Create tag") {
            openGit(localPath).use { git ->
                val cmd = git.tag().setName(tagName)
                if (!message.isNullOrBlank()) cmd.setMessage(message)
                cmd.call()
            }
        }
    }

    suspend fun deleteTag(localPath: String, tagName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingJGit("Delete tag") {
            openGit(localPath).use { git ->
                git.tagDelete().setTags(tagName).call()
            }
        }
    }

    suspend fun getCommitHistory(
        localPath: String,
        maxCount: Int = 50,
        branch: String? = null
    ): Result<List<Commit>> = withContext(Dispatchers.IO) {
        runCatchingJGit("Commit history") {
            openGit(localPath).use { git ->
                val cmd = git.log().setMaxCount(maxCount)
                if (branch != null) {
                    val ref = git.repository.resolve(branch)
                        ?: throw RefNotFoundException("Branch not found: $branch")
                    cmd.add(ref)
                } else {
                    cmd.all()
                }
                cmd.call().map { revCommit ->
                    val author = revCommit.authorIdent
                    Commit(
                        hash = revCommit.name,
                        message = revCommit.fullMessage.trim(),
                        author = author.name,
                        authorLogin = null,
                        authorAvatarUrl = null,
                        timestamp = revCommit.commitTime.toLong() * 1000
                    )
                }
            }
        }
    }

    suspend fun getCurrentBranch(localPath: String): Result<String> = withContext(Dispatchers.IO) {
        runCatchingJGit("Current branch") {
            openGit(localPath).use { git ->
                val branch = git.repository.branch ?: "HEAD"
                if (branch.startsWith(Constants.R_HEADS)) branch.removePrefix(Constants.R_HEADS) else branch
            }
        }
    }

    suspend fun isRepository(localPath: String): Boolean = withContext(Dispatchers.IO) {
        File(localPath, ".git").exists()
    }

    private fun openGit(localPath: String): Git = Git.open(File(localPath))

    private fun <T> runCatchingJGit(operation: String, block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: InvalidRemoteException) {
            Result.failure(GitException("$operation failed: Invalid remote URL", e))
        } catch (e: TransportException) {
            Result.failure(mapTransportError(operation, e))
        } catch (e: RefNotFoundException) {
            Result.failure(GitException("$operation failed: Reference not found", e))
        } catch (e: WrongRepositoryStateException) {
            Result.failure(GitException("$operation failed: Invalid repository state", e))
        } catch (e: NoHeadException) {
            Result.failure(GitException("$operation failed: Repository has no commits yet", e))
        } catch (e: ConcurrentRefUpdateException) {
            Result.failure(GitException("$operation failed: Concurrent update conflict. Try again.", e))
        } catch (e: CanceledException) {
            Result.failure(GitException("$operation was cancelled", e))
        } catch (e: GitAPIException) {
            Result.failure(mapGitApiError(operation, e))
        } catch (e: JGitInternalException) {
            Result.failure(GitException("$operation failed: Internal error", e))
        } catch (e: IOException) {
            Result.failure(mapIoError(operation, e))
        } catch (e: Exception) {
            Result.failure(GitException("$operation failed: ${e.message}", e))
        }
    }

    private fun <T> runCatchingJGit(operation: String, block: () -> Result<T>): Result<T> {
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapException(operation, e))
        }
    }

    private fun mapException(operation: String, e: Exception): GitException {
        return when (e) {
            is TransportException -> mapTransportError(operation, e)
            is GitAPIException -> mapGitApiError(operation, e)
            is IOException -> mapIoError(operation, e)
            else -> GitException("$operation failed: ${e.message}", e)
        }
    }

    private fun mapTransportError(operation: String, e: TransportException): GitException {
        val message = when {
            e.message?.contains("not authorized") == true ||
            e.message?.contains("401") == true ||
            e.message?.contains("Authentication") == true ->
                "Authentication failed. Check your GitHub token."
            e.message?.contains("not found") == true ||
            e.message?.contains("404") == true ->
                "Remote repository not found."
            e.message?.contains("timeout") == true ||
            e.message?.contains("timed out") == true ->
                "Connection timed out. Check your network."
            e.message?.contains("refused") == true ->
                "Connection refused. The remote server may be down."
            else -> "Network error: ${e.message}"
        }
        return GitException(message, e)
    }

    private fun mapGitApiError(operation: String, e: GitAPIException): GitException {
        val message = when {
            e.message?.contains("conflict", ignoreCase = true) == true ||
            e.message?.contains("Conflicting") == true ->
                "$operation failed due to a merge conflict. Resolve conflicts and try again."
            e.message?.contains("not found") == true ->
                "$operation failed: ${e.message}"
            else -> "$operation failed: ${e.message}"
        }
        return GitException(message, e)
    }

    private fun mapIoError(operation: String, e: IOException): GitException {
        val message = when {
            e.message?.contains("Permission denied") == true ->
                "Permission denied. Cannot access the file system."
            e.message?.contains("No space left") == true ->
                "No space left on device. Free up storage and try again."
            e.message?.contains("not a git repository") == true ||
            e.message?.contains("Not a git repository") == true ->
                "Not a Git repository. The directory may not have been cloned yet."
            else -> "File system error: ${e.message}"
        }
        return GitException(message, e)
    }
}

data class PullResultInfo(
    val isSuccessful: Boolean,
    val fetchResult: String,
    val mergeResult: String,
    val rebaseResult: String
)

data class PushResultInfo(
    val isSuccessful: Boolean,
    val messages: String,
    val updates: List<String>
)

data class MergeResultInfo(
    val isSuccessful: Boolean,
    val status: String,
    val newHead: String,
    val conflicts: List<String>
)

class GitException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

private class SimpleProgressMonitor(
    private val onProgress: ((message: String, progress: Int) -> Unit)?
) : ProgressMonitor {

    private var lastMessage: String = ""
    private var totalTasks = 0
    private var completedTasks = 0

    override fun start(totalTasks: Int) {
        this.totalTasks = totalTasks
        this.completedTasks = 0
    }

    override fun beginTask(title: String?, totalWork: Int) {
        lastMessage = title ?: ""
        onProgress?.invoke(lastMessage, 0)
    }

    override fun update(completed: Int) {
        onProgress?.invoke(lastMessage, completed)
    }

    override fun endTask() {
        completedTasks++
        val progress = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 100
        onProgress?.invoke(lastMessage, progress.coerceIn(0, 100))
    }

    override fun isCancelled(): Boolean = false
    override fun showDuration(enabled: Boolean) {}
}
