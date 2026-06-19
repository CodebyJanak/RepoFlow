package com.repoflow.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GithubArtifactDto(
    val id: Long,
    val name: String,
    @SerializedName("size_in_bytes") val sizeInBytes: Long,
    @SerializedName("archive_download_url") val archiveDownloadUrl: String,
    val expired: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("expires_at") val expiresAt: String?
)

data class GithubArtifactListResponse(
    @SerializedName("total_count") val totalCount: Int,
    val artifacts: List<GithubArtifactDto>
)
