# RUN_CATCHING_AUDIT.md

## runCatchingJGit implementation

Located at `core/src/main/java/com/repoflow/core/data/git/GitManager.kt`.

### Overload 1 (line 513)
```kotlin
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
```

### Overload 2 (line 543)
```kotlin
private fun <T> runCatchingJGit(operation: String, block: () -> Result<T>): Result<T> {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(mapException(operation, e))
    }
}
```

---

## Expected signature

```kotlin
private fun <T> runCatchingJGit(operation: String, block: () -> T): Result<T>
```

## Actual signature (what the compiler sees)

After type erasure, both overloads produce:

```
runCatchingJGit(String, Function0) -> Result
```

The JVM platform declaration clash causes the Kotlin compiler to keep only **one** overload — the second one (`block: () -> Result<T>`). All call sites pass `block: () -> T`, which does not match.

---

## Every broken function (22 of 22)

All call sites suffer from the same root cause. The compiler routes every call to the wrong overload.

| Line | Function | Lambda returns | Compiler expects |
|------|----------|---------------|-----------------|
| 49 | `cloneRepository` | `Unit` | `Result<Unit>` |
| 66 | `fetchRepository` | `Unit` | `Result<Unit>` |
| 86 | `pullRepository` | `PullResultInfo` | `Result<PullResultInfo>` |
| 112 | `pushRepository` | `PushResultInfo` | `Result<PushResultInfo>` |
| 137 | `getStatus` | `List<StatusFile>` | `Result<List<StatusFile>>` |
| 170 | `stageFile` | `Unit` | `Result<Unit>` |
| 179 | `stageAll` | `Unit` | `Result<Unit>` |
| 188 | `unstageFile` | `Unit` | `Result<Unit>` |
| 197 | `unstageAll` | `Unit` | `Result<Unit>` |
| 211 | `commit` | `Commit` | `Result<Commit>` |
| 231 | `getLocalBranches` | `List<String>` | `Result<List<String>>` |
| 245 | `createBranch` | `Unit` | `Result<Unit>` |
| 258 | `deleteBranch` | `Unit` | `Result<Unit>` |
| 271 | `renameBranch` | `Unit` | `Result<Unit>` |
| 287 | `checkoutBranch` | `Unit` | `Result<Unit>` |
| 303 | `mergeBranch` | `MergeResultInfo` | `Result<MergeResultInfo>` |
| 320 | `getTags` | `List<Tag>` | `Result<List<Tag>>` |
| 344 | `createTag` | `Unit` | `Result<Unit>` |
| 355 | `deleteTag` | `Unit` | `Result<Unit>` |
| 368 | `getCommitHistory` | `List<Commit>` | `Result<List<Commit>>` |
| 394 | `getCurrentBranch` | `String` | `Result<String>` |
| 411 | `getFileDiff` | `DiffFile` | `Result<DiffFile>` |

---

## Exact fix required

**Remove Overload 2 entirely** (lines 543–551). It is dead code — no call site passes a lambda that returns `Result<T>`.

Overload 1 (`block: () -> T`) correctly handles all 22 call sites. The `mapException` helper used by Overload 2 is also only called from Overload 2, so it would also be removed.

After removal, the compiler sees a single `runCatchingJGit` with `block: () -> T`, type inference chooses `T` correctly for every call, and all type mismatches are eliminated.
