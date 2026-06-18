package com.repoflow.core.data.mapper

import com.repoflow.core.data.local.entity.RepositoryEntity
import com.repoflow.core.data.remote.dto.GithubBranchDto
import com.repoflow.core.data.remote.dto.GithubCommitDto
import com.repoflow.core.data.remote.dto.GithubContributorDto
import com.repoflow.core.data.remote.dto.GithubReleaseDto
import com.repoflow.core.data.remote.dto.GithubRepoDto
import com.repoflow.core.data.remote.dto.GithubUserDto
import com.repoflow.core.domain.model.Branch
import com.repoflow.core.domain.model.Commit
import com.repoflow.core.domain.model.Contributor
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.domain.model.Release
import com.repoflow.core.domain.model.User
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object RepositoryMapper {

    fun GithubUserDto.toDomain(): User = User(
        id = id,
        login = login,
        avatarUrl = avatarUrl,
        name = name,
        email = email,
        bio = bio,
        publicRepos = publicRepos
    )

    fun GithubRepoDto.toDomain(): GitRepository = GitRepository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        isPrivate = isPrivate,
        stars = stargazersCount,
        forks = forksCount,
        language = language ?: "",
        openIssues = openIssuesCount,
        defaultBranch = defaultBranch,
        owner = User(
            id = owner.id,
            login = owner.login,
            avatarUrl = owner.avatarUrl,
            name = null,
            email = null,
            bio = null,
            publicRepos = 0
        )
    )

    fun RepositoryEntity.toDomain(): GitRepository = GitRepository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        isPrivate = isPrivate,
        stars = stars,
        forks = forks,
        language = language ?: "",
        openIssues = openIssues,
        defaultBranch = defaultBranch,
        owner = User(
            id = 0,
            login = ownerLogin,
            avatarUrl = ownerAvatarUrl,
            name = null,
            email = null,
            bio = null,
            publicRepos = 0
        ),
        isFavorite = isFavorite,
        isCloned = isCloned,
        localPath = localPath
    )

    fun GitRepository.toEntity(): RepositoryEntity = RepositoryEntity(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        isPrivate = isPrivate,
        stars = stars,
        forks = forks,
        language = language,
        openIssues = openIssues,
        defaultBranch = defaultBranch,
        ownerLogin = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        isFavorite = isFavorite,
        isCloned = isCloned,
        localPath = localPath,
        lastSyncedAt = System.currentTimeMillis()
    )

    fun GithubBranchDto.toDomain(): Branch = Branch(
        name = name,
        sha = commit.sha,
        isProtected = isProtected
    )

    fun GithubCommitDto.toDomain(): Commit {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val timestamp = try {
            dateFormat.parse(commit.author.date)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
        return Commit(
            hash = sha,
            message = commit.message,
            author = commit.author.name,
            authorLogin = author?.login,
            authorAvatarUrl = author?.avatarUrl,
            timestamp = timestamp
        )
    }

    fun GithubContributorDto.toDomain(): Contributor = Contributor(
        login = login,
        avatarUrl = avatarUrl,
        contributions = contributions
    )

    fun GithubReleaseDto.toDomain(): Release = Release(
        id = id,
        tagName = tagName,
        name = name,
        body = body,
        isPrerelease = prerelease,
        createdAt = createdAt,
        publishedAt = publishedAt,
        htmlUrl = htmlUrl
    )
}
