# GIT_MANAGER_FIX_REPORT

## Root Cause

The `GitManager.kt` file had two categories of compilation errors:

### 1. Return type mismatches (`Result<Unit>` vs JGit actual types)

11 functions declared `Result<Unit>` but their `runCatchingJGit` lambda blocks returned the raw JGit result object instead of `Unit`. Kotlin's type inference inferred `T` as the JGit type (e.g., `DirCache`, `Ref`, `FetchResult`, `List<String>`), making the function effectively return `Result<DirCache>`, `Result<Ref>`, etc. — which did not match the declared `Result<Unit>` return type.

### 2. Mixed return type in `getFileDiff`

The `use { }` block returned two different types:
- `return@use Result.failure(GitException(...))` → `Result<DiffFile>`
- `parseDiff(...)` → `DiffFile`

This caused the block's inferred type to be `Any`, and the outer `runCatchingJGit` returned `Result<Any>`, not `Result<DiffFile>`.

### 3. `.name()` invoked as a function instead of a property

Three locations called `.name()` on Java enum values (`RebaseResult.Status`, `RemoteRefUpdate.Status`, `MergeStatus`). While `Enum.name()` is accessible as both `.name` and `.name()` in Kotlin, property access is the idiomatic Kotlin form.

---

## Functions Fixed

### `fetchRepository`
- **Before**: `git.fetch()...call()` returned `FetchResult`
- **After**: Added `Unit` after `.call()` to make the lambda return `Unit`

### `stageFile`
- **Before**: `git.add()...call()` returned `DirCache`
- **After**: Added `Unit` after `.call()`

### `stageAll`
- **Before**: `git.add()...call()` returned `DirCache`
- **After**: Added `Unit` after `.call()`

### `unstageFile`
- **Before**: `git.reset()...call()` returned `Ref`
- **After**: Added `Unit` after `.call()`

### `unstageAll`
- **Before**: `git.reset().call()` returned `Ref`
- **After**: Added `Unit` after `.call()`

### `createBranch`
- **Before**: `git.branchCreate()...call()` returned `Ref`
- **After**: Added `Unit` after `.call()`

### `deleteBranch`
- **Before**: `git.branchDelete()...call()` returned `List<String>`
- **After**: Added `Unit` after `.call()`

### `renameBranch`
- **Before**: `git.branchRename()...call()` returned `Ref`
- **After**: Added `Unit` after `.call()`

### `checkoutBranch`
- **Before**: `git.checkout()...call()` returned `Ref`
- **After**: Added `Unit` after `.call()`

### `createTag`
- **Before**: `cmd.call()` returned `Ref`
- **After**: Added `Unit` after `cmd.call()`

### `deleteTag`
- **Before**: `git.tagDelete()...call()` returned `List<String>`
- **After**: Added `Unit` after `.call()`

### `getFileDiff`
- **Before**: `return@use Result.failure(GitException(...))` returned `Result<DiffFile>` from the `use` block, which mixed with the `DiffFile` return of `parseDiff()`
- **After**: Changed to `throw GitException(...)` — the exception is now caught by `runCatchingJGit`'s catch handler, and the `use` block always returns `DiffFile` consistently

### `.name()` → `.name` (3 locations)
- Line 100: `status?.name()` → `status?.name`
- Line 129: `it.status.name()` → `it.status.name`
- Line 311: `mergeResult.mergeStatus.name()` → `mergeResult.mergeStatus.name`

---

## Scan Results (Other Git-Related Files)

| File | Finding |
|------|---------|
| `GitRepositoryImpl.kt` | All `Result` types match correctly. `.map {}` / `.mapCatching {}` properly converts `PullResultInfo`/`PushResultInfo`/`MergeResultInfo` to `Unit`. |
| `GitRepository.kt` (domain interface) | No issues — all return types are correct. |
| `CloneUseCase.kt` | No issues. |
| `CommitUseCase.kt` | No issues. |
| `BranchUseCase.kt` | No issues. `runCatching` on line 45 uses Kotlin stdlib correctly. |
| `HistoryUseCase.kt` | No issues. |
| `SyncUseCase.kt` | No issues. |
| `TagUseCase.kt` | No issues. |
| DTO files, mappers, models | No issues found. |

---

## Why Compilation Failed

Kotlin enforces strict type checking on `Result<T>`. When `runCatchingJGit { ... }` is called, the compiler infers `T` from the lambda's return type. If the lambda returns `DirCache` but the function declares `Result<Unit>`, Kotlin sees:

```
Type mismatch: inferred type is Result<DirCache> but Result<Unit> was expected
```

Similarly, mixing `DiffFile` and `Result<DiffFile>` in the same block produces a type that cannot satisfy the declared `Result<DiffFile>`, causing a cascading type error.

The `.name()` calls caused no explicit compilation error (both forms are valid for Java enums in Kotlin), but the user reported them as detected problems. These were fixed to use idiomatic property access.
