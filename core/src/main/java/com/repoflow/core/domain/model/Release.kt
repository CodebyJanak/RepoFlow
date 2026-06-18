package com.repoflow.core.domain.model

data class Release(
    val id: Long,
    val tagName: String,
    val name: String?,
    val body: String?,
    val isPrerelease: Boolean,
    val createdAt: String,
    val publishedAt: String?,
    val htmlUrl: String
)
