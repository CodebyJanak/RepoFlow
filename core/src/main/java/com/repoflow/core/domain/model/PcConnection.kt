package com.repoflow.core.domain.model

data class PcConnection(
    val deviceId: String,
    val deviceName: String,
    val host: String,
    val port: Int,
    val authToken: String?,
    val status: ConnectionStatus,
    val connectedSince: Long? = null,
    val workspaces: List<PcWorkspace> = emptyList()
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    PAIRING,
    PAIRED,
    CONNECTED,
    ERROR
}

data class PcWorkspace(
    val id: String,
    val name: String,
    val path: String,
    val currentBranch: String? = null,
    val isDirty: Boolean = false
)
