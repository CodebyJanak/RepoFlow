package com.repoflow.core.domain.model

data class PcDevice(
    val deviceId: String,
    val deviceName: String,
    val host: String,
    val port: Int,
    val protocolVersion: String,
    val requiresPairing: Boolean,
    val isPaired: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)
