package com.repoflow.core.domain.model

data class Workflow(
    val id: Long,
    val name: String,
    val path: String,
    val state: String,
    val createdAt: String,
    val updatedAt: String,
    val htmlUrl: String,
    val badgeUrl: String
)
