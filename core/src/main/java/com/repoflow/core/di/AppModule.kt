package com.repoflow.core.di

import com.repoflow.core.data.repository.ActionsRepositoryImpl
import com.repoflow.core.data.repository.ActivityRepositoryImpl
import com.repoflow.core.data.repository.PcBridgeRepositoryImpl
import com.repoflow.core.data.repository.AuthRepositoryImpl
import com.repoflow.core.data.repository.GitRepositoryImpl
import com.repoflow.core.data.repository.IssuesRepositoryImpl
import com.repoflow.core.data.repository.PullRequestsRepositoryImpl
import com.repoflow.core.data.repository.SettingsRepositoryImpl
import com.repoflow.core.data.repository.WorkspaceRepositoryImpl
import com.repoflow.core.domain.repository.ActionsRepository
import com.repoflow.core.domain.repository.ActivityRepository
import com.repoflow.core.domain.repository.PcBridgeRepository
import com.repoflow.core.domain.repository.AuthRepository
import com.repoflow.core.domain.repository.GitRepository
import com.repoflow.core.domain.repository.IssuesRepository
import com.repoflow.core.domain.repository.PullRequestsRepository
import com.repoflow.core.domain.repository.SettingsRepository
import com.repoflow.core.domain.repository.WorkspaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGitRepository(impl: GitRepositoryImpl): GitRepository

    @Binds
    @Singleton
    abstract fun bindWorkspaceRepository(impl: WorkspaceRepositoryImpl): WorkspaceRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindIssuesRepository(impl: IssuesRepositoryImpl): IssuesRepository

    @Binds
    @Singleton
    abstract fun bindPullRequestsRepository(impl: PullRequestsRepositoryImpl): PullRequestsRepository

    @Binds
    @Singleton
    abstract fun bindActionsRepository(impl: ActionsRepositoryImpl): ActionsRepository

    @Binds
    @Singleton
    abstract fun bindPcBridgeRepository(impl: PcBridgeRepositoryImpl): PcBridgeRepository
}
