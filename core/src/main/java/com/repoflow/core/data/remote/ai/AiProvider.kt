package com.repoflow.core.data.remote.ai

interface AiProvider {
    suspend fun generate(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 1024
    ): Result<String>
}
