package com.repoflow.core.data.remote.pc

import com.google.gson.annotations.SerializedName

data class PcBeaconMessage(
    @SerializedName("type") val type: String,
    @SerializedName("deviceName") val deviceName: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("protocolVersion") val protocolVersion: String,
    @SerializedName("port") val port: Int,
    @SerializedName("requiresPairing") val requiresPairing: Boolean = true
)

data class PcProbeMessage(
    @SerializedName("type") val type: String = "probe",
    @SerializedName("clientName") val clientName: String,
    @SerializedName("protocolVersion") val protocolVersion: String
)

data class PcRequestMessage(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String = "request",
    @SerializedName("command") val command: String,
    @SerializedName("params") val params: Map<String, Any?> = emptyMap(),
    @SerializedName("authToken") val authToken: String? = null
)

data class PcResponseMessage(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Map<String, Any?>? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)

data class PcEventMessage(
    @SerializedName("type") val type: String = "event",
    @SerializedName("event") val event: String,
    @SerializedName("data") val data: Map<String, Any?>? = null
)
