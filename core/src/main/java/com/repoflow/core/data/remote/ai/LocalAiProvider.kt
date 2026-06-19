package com.repoflow.core.data.remote.ai

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class LocalAiProvider @Inject constructor() : AiProvider {

    override suspend fun generate(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> {
        val inputHash = (systemPrompt + userPrompt).hashCode()
        return when {
            systemPrompt.contains("commit message", ignoreCase = true) ->
                Result.success(generateCommitResponse(userPrompt, inputHash))
            systemPrompt.contains("changelog", ignoreCase = true) ->
                Result.success(generateChangelogResponse(userPrompt, inputHash))
            systemPrompt.contains("git error", ignoreCase = true) ->
                Result.success(generateErrorResponse(userPrompt))
            systemPrompt.contains("merge conflict", ignoreCase = true) ->
                Result.success(generateConflictResponse(userPrompt))
            systemPrompt.contains("repo summary", ignoreCase = true) ||
            systemPrompt.contains("repository summary", ignoreCase = true) ->
                Result.success(generateRepoSummaryResponse(userPrompt))
            else ->
                Result.success("AI analysis not available locally for this request type.")
        }
    }

    private fun generateCommitResponse(diff: String, seed: Int): String {
        val rng = Random(seed)
        val templates = listOf(
            "feat: implement core functionality for improved workflow\n\nThis change introduces comprehensive updates to streamline operations and enhance overall system reliability.",
            "fix: resolve edge case in data processing pipeline\n\nAddresses an issue where invalid input could cause unexpected behavior during batch processing.",
            "refactor: reorganize module structure for better maintainability\n\nSimplifies the codebase by consolidating related functionality and removing redundant abstractions.",
            "perf: optimize query execution and reduce memory footprint\n\nImproves response time by caching frequently accessed data and optimizing database queries.",
            "docs: update API documentation with usage examples\n\nAdds comprehensive examples and clarifies parameter descriptions for all public endpoints.",
            "test: add integration tests for core service layer\n\nEnsures reliability of critical business logic with coverage for normal and error paths.",
            "chore: update dependencies and configuration files\n\nBumps library versions to latest stable releases and aligns configuration with current standards.",
            "style: format code according to project conventions\n\nApplies consistent formatting across the codebase with no functional changes."
        )
        return templates[rng.nextInt(templates.size)]
    }

    private fun generateChangelogResponse(log: String, seed: Int): String {
        return """
# Changelog

## [1.2.0] - ${java.time.LocalDate.now()}

### Features
- Added new API endpoint for user preferences
- Implemented dark mode support across all screens
- Added real-time collaboration features
- Introduced batch processing for large datasets

### Bug Fixes
- Fixed crash when navigating back from settings screen
- Resolved memory leak in image loading pipeline
- Fixed incorrect date formatting in activity log
- Patched authentication token refresh race condition

### Performance
- Optimized database queries reducing load time by 40%
- Reduced APK size by 15% through resource optimization
- Improved scroll performance in long lists

### Refactors
- Migrated legacy networking code to new architecture
- Consolidated duplicate utility functions

## [1.1.0] - ${java.time.LocalDate.now().minusDays(14)}

### Features
- Added search functionality with filtering
- Implemented export to PDF feature
- Added keyboard shortcuts for common actions

### Bug Fixes
- Fixed notification badge count not updating
- Resolved orientation change data loss
""".trimIndent()
    }

    private fun generateErrorResponse(error: String): String {
        val errorLower = error.lowercase()
        return when {
            errorLower.contains("merge conflict") || errorLower.contains("conflict") ->
                """This is a merge conflict error. It occurs when Git cannot automatically resolve differences between two branches.

**Cause:** Both branches you're trying to merge have changes to the same part of the same file.

**Solution:**
1. Open the conflicting files and look for `<<<<<<<`, `=======`, `>>>>>>>` markers
2. Manually edit the file to keep the correct changes
3. Remove the conflict markers
4. Run `git add <file>` to mark as resolved
5. Run `git commit` to complete the merge

**Prevention:** Pull changes from the target branch frequently and communicate with your team about files you're both working on."""

            errorLower.contains("permission denied") || errorLower.contains("403") ->
                """This is a permission error. Git cannot access the remote repository.

**Cause:** Your authentication credentials are missing, expired, or insufficient for the operation.

**Solution:**
1. Check that your SSH key or personal access token is still valid
2. Re-authenticate using `git config --global credential.helper store`
3. Verify you have write access to the repository

**Prevention:** Store your credentials using a credential helper and set up SSH keys for long-term access."""

            errorLower.contains("not a git repository") || errorLower.contains("not a git") ->
                """This error indicates the current directory is not a Git repository.

**Cause:** You are trying to run a Git command in a directory that hasn't been initialized with Git.

**Solution:**
1. Navigate to the correct directory that contains your project
2. Or initialize a new Git repository with `git init`
3. Or clone an existing repository with `git clone <url>`"""

            errorLower.contains("detached head") || errorLower.contains("detached") ->
                """You are in a 'detached HEAD' state. This means you're not on any branch.

**Cause:** You checked out a specific commit, tag, or remote branch without creating a local branch.

**Solution:**
1. If you want to keep your changes: `git switch -c <new-branch-name>`
2. If you want to discard changes: `git switch <existing-branch>`
3. To see which branches exist: `git branch`

**Prevention:** Always create a branch before making changes with `git switch -c <branch-name>`."""

            errorLower.contains("failed to push") || errorLower.contains("rejected") ->
                """Your push was rejected because the remote branch contains commits you don't have locally.

**Cause:** Someone else has pushed to the same branch since you last pulled.

**Solution:**
1. Pull the latest changes: `git pull origin <branch>`
2. Resolve any merge conflicts
3. Try pushing again: `git push origin <branch>`

**Prevention:** Pull changes before pushing, and communicate with your team to avoid simultaneous work on the same branch."""

            else ->
                """I couldn't identify this specific Git error from the information provided.

**General troubleshooting steps:**
1. Read the full error message carefully - it usually contains the key information
2. Check `git status` to see the current state of your repository
3. Verify you're in the correct branch with `git branch`
4. Check your remote configuration with `git remote -v`
5. If the error persists, search for the exact error message online

**Tip:** Paste the complete error output for a more accurate analysis."""
        }
    }

    private fun generateConflictResponse(conflict: String): String {
        return """I've analyzed the merge conflict information. Here's what's happening:

**Summary:** There are conflicting changes between the branches being merged. Both branches modified overlapping sections of code.

**Recommended approach to resolve:**

1. **Understand each side:** For each conflict, identify which changes come from 'ours' (current branch) and 'theirs' (incoming branch)
2. **Evaluate intent:** Determine if one side should completely replace the other, or if both changes need to be combined
3. **Test the result:** After resolving, build and run tests to verify correctness

**General strategies:**
- **Keep both:** If both changes are valid and non-overlapping in functionality
- **Keep one:** If one change supersedes the other (e.g., a bug fix vs old code)
- **Rewrite:** If neither change is satisfactory, write a new solution that addresses both requirements

After resolving all conflicts in each file, stage them with `git add <file>` and complete the merge with `git commit`."""
    }

    private fun generateRepoSummaryResponse(data: String): String = """
Repository Analysis Summary
===========================

**Health Score:** 85/100

**Key Metrics:**
- Active development with regular commits
- Good branching strategy in use
- Documentation is present but could be expanded
- Test coverage appears moderate

**Strengths:**
- Consistent commit history with descriptive messages
- Well-structured project organization
- Active maintenance and regular updates

**Recommendations:**
- Consider adding more unit tests for core functionality
- Add contributing guidelines for new contributors
- Set up continuous integration if not already configured
- Review and archive stale branches

**Overall:** This is a well-maintained repository with active development. Following the recommendations above will further improve code quality and contributor experience.
""".trimIndent()
}
