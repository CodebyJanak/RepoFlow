package com.repoflow.core.data.mapper

import com.repoflow.core.data.local.entity.RepositoryEntity
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

    fun RepositoryEntity.toDomain(): GitRepository = GitRepository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        isPrivate = isPrivate,
        stars = stars,
        forks = forks,
        openIssues = 0,
        defaultBranch = defaultBranch,
        owner = User(
            id = 0,
            login = fullName.substringBefore("/"),
            avatarUrl = "",
            name = null,
            email = null,
            bio = null,
            publicRepos = 0
        ),
        isCloned = localPath != null,
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
        defaultBranch = defaultBranch,
        localPath = localPath,
        lastSyncedAt = System.currentTimeMillis()
    )
}
