package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubReleaseDto(
    val id: Long,
    @SerializedName("tag_name")
    val tagName: String,
    val name: String?,
    val body: String?,
    val prerelease: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("published_at")
    val publishedAt: String?,
    @SerializedName("html_url")
    val htmlUrl: String
)
