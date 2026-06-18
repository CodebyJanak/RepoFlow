package com.repoflow.core.di

import com.repoflow.core.data.repository.ActivityRepositoryImpl
import com.repoflow.core.data.repository.AuthRepositoryImpl
import com.repoflow.core.data.repository.GitRepositoryImpl
import com.repoflow.core.data.repository.SettingsRepositoryImpl
import com.repoflow.core.data.repository.WorkspaceRepositoryImpl
import com.repoflow.core.domain.repository.ActivityRepository
import com.repoflow.core.domain.repository.AuthRepository
import com.repoflow.core.domain.repository.GitRepository
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
}
