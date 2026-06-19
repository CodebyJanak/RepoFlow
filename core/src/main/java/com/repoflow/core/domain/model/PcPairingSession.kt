package com.repoflow.core.domain.model

data class PcPairingSession(
    val sessionId: String,
    val deviceId: String,
    val pairingCode: String,
    val qrContent: String,
    val createdAt: Long,
    val expiresAt: Long,
    val status: PairingStatus
)

enum class PairingStatus {
    PENDING,
    SCANNED,
    VERIFIED,
    COMPLETED,
    EXPIRED,
    FAILED
}
