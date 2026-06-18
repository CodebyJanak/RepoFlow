package com.repoflow.core.data.mapper

import com.repoflow.core.data.local.entity.RepositoryEntity
import com.repoflow.core.data.remote.dto.GithubRepoDto
import com.repoflow.core.data.remote.dto.GithubUserDto
import com.repoflow.core.domain.model.GitRepository
import com.repoflow.core.domain.model.User

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
}
