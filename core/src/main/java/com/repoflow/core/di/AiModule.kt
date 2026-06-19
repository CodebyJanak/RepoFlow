package com.repoflow.core.di

import com.repoflow.core.data.remote.ai.AiProvider
import com.repoflow.core.data.remote.ai.LocalAiProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiProvider(impl: LocalAiProvider): AiProvider
}
