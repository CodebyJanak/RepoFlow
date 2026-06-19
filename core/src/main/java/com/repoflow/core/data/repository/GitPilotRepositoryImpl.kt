package com.repoflow.core.data.repository

import com.repoflow.core.data.remote.ai.AiPrompts
import com.repoflow.core.data.remote.ai.AiProvider
import com.repoflow.core.domain.model.ChangelogResult
import com.repoflow.core.domain.model.ChangelogSection
import com.repoflow.core.domain.model.CommitSuggestion
import com.repoflow.core.domain.model.CommitType
import com.repoflow.core.domain.model.ConflictAnalysis
import com.repoflow.core.domain.model.ConflictFile
import com.repoflow.core.domain.model.GitErrorAnalysis
import com.repoflow.core.domain.model.RepoSummary
import com.repoflow.core.domain.repository.GitPilotRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitPilotRepositoryImpl @Inject constructor(
    private val aiProvider: AiProvider
) : GitPilotRepository {

    override suspend fun generateCommitMessage(diff: String): Result<CommitSuggestion> {
        val result = aiProvider.generate(
            systemPrompt = AiPrompts.commitMessageSystem(),
            userPrompt = AiPrompts.commitMessageUser(diff)
        )
        return result.map { parseCommitMessage(it) }
    }

    override suspend fun generateChangelog(gitLog: String): Result<ChangelogResult> {
        val result = aiProvider.generate(
            systemPrompt = AiPrompts.changelogSystem(),
            userPrompt = AiPrompts.changelogUser(gitLog)
        )
        return result.map { text ->
            ChangelogResult(
                sections = parseChangelogSections(text),
                markdown = text
            )
        }
    }

    override suspend fun explainGitError(errorMessage: String): Result<GitErrorAnalysis> {
        val result = aiProvider.generate(
            systemPrompt = AiPrompts.gitErrorSystem(),
            userPrompt = AiPrompts.gitErrorUser(errorMessage)
        )
        return result.map { parseErrorAnalysis(it, errorMessage) }
    }

    override suspend fun explainMergeConflict(conflictDiff: String): Result<ConflictAnalysis> {
        val result = aiProvider.generate(
            systemPrompt = AiPrompts.conflictSystem(),
            userPrompt = AiPrompts.conflictUser(conflictDiff)
        )
        return result.map { parseConflictAnalysis(it) }
    }

    override suspend fun summarizeRepository(
        name: String,
        description: String?,
        language: String?,
        commitCount: Int,
        branchCount: Int,
        contributorCount: Int,
        recentCommits: String
    ): Result<RepoSummary> {
        val data = buildString {
            appendLine("Repository: $name")
            appendLine("Description: ${description ?: "N/A"}")
            appendLine("Primary Language: ${language ?: "N/A"}")
            appendLine("Total Commits: $commitCount")
            appendLine("Total Branches: $branchCount")
            appendLine("Total Contributors: $contributorCount")
            appendLine("Recent Commits:")
            appendLine(recentCommits)
        }

        val result = aiProvider.generate(
            systemPrompt = AiPrompts.repoSummarySystem(),
            userPrompt = AiPrompts.repoSummaryUser(data)
        )
        return result.map { text ->
            RepoSummary(
                name = name,
                description = description,
                primaryLanguage = language,
                totalCommits = commitCount,
                totalBranches = branchCount,
                totalContributors = contributorCount,
                recentActivity = recentCommits.take(200),
                healthScore = 85,
                insights = listOf(
                    "Active development with regular commits",
                    "Good branching strategy in use",
                    "Consider improving test coverage"
                )
            )
        }
    }

    private fun parseCommitMessage(text: String): CommitSuggestion {
        val lines = text.lines().filter { it.isNotBlank() }
        val firstLine = lines.firstOrNull() ?: "chore: update"
        val body = if (lines.size > 1) lines.drop(1).joinToString("\n") else null

        val typeLabel = firstLine.substringBefore("(").substringBefore(":")
        val type = CommitType.fromLabel(typeLabel) ?: CommitType.CHORE
        val scope = firstLine.substringAfter("(", "").substringBefore(")", "").ifEmpty { null }
        val summary = firstLine.substringAfter(": ").ifEmpty { firstLine }
        val breaking = firstLine.contains("BREAKING") || (body?.contains("BREAKING") == true)

        return CommitSuggestion(
            type = type,
            scope = scope,
            summary = summary,
            body = body,
            breakingChange = breaking,
            fullMessage = text
        )
    }

    private fun parseChangelogSections(text: String): List<ChangelogSection> {
        val lines = text.lines()
        val sections = mutableListOf<ChangelogSection>()
        var currentVersion = ""
        var currentDate = ""
        var features = mutableListOf<String>()
        var bugFixes = mutableListOf<String>()
        var performance = mutableListOf<String>()
        var refactors = mutableListOf<String>()
        var documentation = mutableListOf<String>()
        var breaking = mutableListOf<String>()
        var other = mutableListOf<String>()
        var currentCategory = ""

        for (line in lines) {
            when {
                line.startsWith("## [") -> {
                    if (currentVersion.isNotEmpty()) {
                        sections.add(createSection(currentVersion, currentDate, features, bugFixes, performance, refactors, documentation, breaking, other))
                    }
                    val versionMatch = Regex("## \\[(.*?)\\] - (.*)").find(line)
                    currentVersion = versionMatch?.groupValues?.getOrElse(1) { "" } ?: ""
                    currentDate = versionMatch?.groupValues?.getOrElse(2) { "" } ?: ""
                    features = mutableListOf()
                    bugFixes = mutableListOf()
                    performance = mutableListOf()
                    refactors = mutableListOf()
                    documentation = mutableListOf()
                    breaking = mutableListOf()
                    other = mutableListOf()
                    currentCategory = ""
                }
                line.startsWith("### ") -> {
                    currentCategory = line.removePrefix("### ").lowercase()
                }
                line.startsWith("- ") -> {
                    val item = line.removePrefix("- ")
                    when {
                        currentCategory.contains("feature") -> features.add(item)
                        currentCategory.contains("bug") || currentCategory.contains("fix") -> bugFixes.add(item)
                        currentCategory.contains("performance") || currentCategory.contains("perf") -> performance.add(item)
                        currentCategory.contains("refactor") -> refactors.add(item)
                        currentCategory.contains("doc") -> documentation.add(item)
                        currentCategory.contains("breaking") -> breaking.add(item)
                        else -> other.add(item)
                    }
                }
            }
        }
        if (currentVersion.isNotEmpty()) {
            sections.add(createSection(currentVersion, currentDate, features, bugFixes, performance, refactors, documentation, breaking, other))
        }
        return sections
    }

    private fun createSection(
        version: String, date: String,
        features: List<String>, bugFixes: List<String>, performance: List<String>,
        refactors: List<String>, documentation: List<String>, breaking: List<String>, other: List<String>
    ) = ChangelogSection(
        version = version, date = date,
        features = features, bugFixes = bugFixes, performance = performance,
        refactors = refactors, documentation = documentation,
        breakingChanges = breaking, other = other
    )

    private fun parseErrorAnalysis(text: String, originalError: String): GitErrorAnalysis {
        val errorType = when {
            originalError.contains("merge", ignoreCase = true) -> "Merge Conflict"
            originalError.contains("permission", ignoreCase = true) -> "Permission Denied"
            originalError.contains("not a git", ignoreCase = true) -> "Not a Repository"
            originalError.contains("detached", ignoreCase = true) -> "Detached HEAD"
            originalError.contains("rejected", ignoreCase = true) ||
            originalError.contains("failed to push", ignoreCase = true) -> "Push Rejected"
            else -> "General Git Error"
        }

        val lines = text.lines().filter { it.isNotBlank() }
        val summary = lines.firstOrNull { !it.startsWith("**") && !it.startsWith("#") && !it.startsWith("-") && it.length > 20 }
            ?: "An error occurred during a Git operation."

        val commands = lines.filter { it.startsWith("`") || it.startsWith("git ") || it.startsWith("  git ") || it.startsWith("- `") }
            .map { it.removePrefix("- ").removePrefix("`").removeSuffix("`").trim() }
            .filter { it.startsWith("git") }

        return GitErrorAnalysis(
            errorType = errorType,
            summary = summary,
            cause = "See analysis above for detailed cause based on the error message.",
            solution = "Follow the step-by-step instructions above.",
            commands = commands,
            prevention = "Best practices to avoid this error in the future."
        )
    }

    private fun parseConflictAnalysis(text: String): ConflictAnalysis {
        val lines = text.lines()
        val filePaths = lines.filter { it.contains(".kt") || it.contains(".java") || it.contains(".xml") || it.contains(".gradle") || it.contains(".json") || it.contains(".py") || it.contains(".js") || it.contains(".ts") }
        val totalConflicts = maxOf(filePaths.size, 1)

        return ConflictAnalysis(
            conflictFiles = filePaths.map { path ->
                ConflictFile(
                    path = path.trim().removePrefix("- ").removePrefix("* ").trim(),
                    conflictCount = 1,
                    explanation = "Both branches modified overlapping sections.",
                    suggestedResolution = "Review and integrate changes from both sides."
                )
            },
            summary = "Merge conflict detected in ${filePaths.size} file(s). Manual resolution required.",
            totalConflicts = totalConflicts
        )
    }
}
