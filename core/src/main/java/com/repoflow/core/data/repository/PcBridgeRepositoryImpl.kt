package com.repoflow.core.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.repoflow.core.data.remote.pc.PcDiscoveryService
import com.repoflow.core.data.remote.pc.PcWebSocketClient
import com.repoflow.core.domain.model.ConnectionStatus
import com.repoflow.core.domain.model.PcCommand
import com.repoflow.core.domain.model.PcCommandResult
import com.repoflow.core.domain.model.PcConnection
import com.repoflow.core.domain.model.PcDevice
import com.repoflow.core.domain.model.PcWorkspace
import com.repoflow.core.domain.repository.PcBridgeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PcBridgeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val discoveryService: PcDiscoveryService,
    private val webSocketClient: PcWebSocketClient
) : PcBridgeRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _discoveredDevices = MutableStateFlow<List<PcDevice>>(emptyList())
    override val discoveredDevices: Flow<List<PcDevice>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(
        PcConnection(
            deviceId = "",
            deviceName = "",
            host = "",
            port = 0,
            authToken = null,
            status = ConnectionStatus.DISCONNECTED
        )
    )
    override val connectionState: Flow<PcConnection> = _connectionState.asStateFlow()

    private var currentDevice: PcDevice? = null
    private var authToken: String? = null

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "pc_bridge_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun startDiscovery() {
        scope.launch {
            discoveryService.startListening().collect { device ->
                if (device.deviceId.isNotEmpty()) {
                    val current = _discoveredDevices.value.toMutableList()
                    val index = current.indexOfFirst { it.deviceId == device.deviceId }
                    if (index >= 0) {
                        current[index] = device.copy(isPaired = current[index].isPaired)
                    } else {
                        current.add(device.copy(isPaired = isDevicePaired(device.deviceId)))
                    }
                    _discoveredDevices.value = current
                }
            }
        }
        discoveryService.sendProbe("RepoFlow-Android")
    }

    override suspend fun stopDiscovery() {
        discoveryService.stopListening()
    }

    override suspend fun connect(device: PcDevice): Result<PcConnection> {
        return try {
            currentDevice = device
            _connectionState.value = _connectionState.value.copy(
                deviceId = device.deviceId,
                deviceName = device.deviceName,
                host = device.host,
                port = device.port,
                status = ConnectionStatus.CONNECTING
            )

            val savedToken = getSavedToken(device.deviceId)
            authToken = savedToken

            if (savedToken != null) {
                webSocketClient.connect(
                    host = device.host,
                    port = device.port,
                    onConnected = {
                        _connectionState.value = _connectionState.value.copy(
                            status = ConnectionStatus.CONNECTED,
                            connectedSince = System.currentTimeMillis()
                        )
                        // Verify auth with ping
                        scope.launch {
                            verifyAuth()
                        }
                    },
                    onDisconnected = { reason ->
                        _connectionState.value = _connectionState.value.copy(
                            status = ConnectionStatus.DISCONNECTED
                        )
                    }
                )
                Result.success(_connectionState.value)
            } else {
                _connectionState.value = _connectionState.value.copy(
                    status = ConnectionStatus.PAIRING
                )
                Result.success(_connectionState.value)
            }
        } catch (e: Exception) {
            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.ERROR
            )
            Result.failure(e)
        }
    }

    override suspend fun pair(
        device: PcDevice,
        pairingToken: String,
        deviceName: String,
        deviceId: String
    ): Result<PcConnection> {
        return try {
            webSocketClient.connect(
                host = device.host,
                port = device.port,
                onConnected = { },
                onDisconnected = { }
            )

            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.PAIRING
            )

            val result = webSocketClient.sendCommand(
                PcCommand.Pair(
                    method = "qr",
                    pairingToken = pairingToken,
                    deviceName = deviceName,
                    deviceId = deviceId
                )
            )

            when (result) {
                is PcCommandResult.PairResult -> {
                    authToken = result.authToken
                    saveToken(device.deviceId, result.authToken)

                    val connection = _connectionState.value.copy(
                        deviceId = device.deviceId,
                        deviceName = device.deviceName,
                        host = device.host,
                        port = device.port,
                        authToken = result.authToken,
                        status = ConnectionStatus.CONNECTED,
                        connectedSince = System.currentTimeMillis(),
                        workspaces = result.workspaces
                    )
                    _connectionState.value = connection
                    markDevicePaired(device.deviceId)

                    Result.success(connection)
                }
                is PcCommandResult.Error -> {
                    _connectionState.value = _connectionState.value.copy(
                        status = ConnectionStatus.ERROR
                    )
                    Result.failure(Exception("${result.code}: ${result.message}"))
                }
                else -> {
                    _connectionState.value = _connectionState.value.copy(
                        status = ConnectionStatus.ERROR
                    )
                    Result.failure(Exception("Unexpected pairing result"))
                }
            }
        } catch (e: Exception) {
            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.ERROR
            )
            Result.failure(e)
        }
    }

    override suspend fun disconnect() {
        webSocketClient.disconnect()
        currentDevice = null
        _connectionState.value = PcConnection(
            deviceId = "", deviceName = "", host = "", port = 0,
            authToken = null, status = ConnectionStatus.DISCONNECTED
        )
    }

    override suspend fun executeCommand(command: PcCommand): Result<PcCommandResult> {
        return try {
            val result = webSocketClient.sendCommand(command, authToken)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPairedDevices(): List<PcDevice> {
        val pairedIds = prefs.getStringSet("paired_devices", emptySet()) ?: emptySet()
        return _discoveredDevices.value.filter { it.deviceId in pairedIds }
    }

    override suspend fun forgetDevice(deviceId: String) {
        val pairedIds = prefs.getStringSet("paired_devices", emptySet())?.toMutableSet() ?: mutableSetOf()
        pairedIds.remove(deviceId)
        prefs.edit().putStringSet("paired_devices", pairedIds).apply()
        prefs.edit().remove("token_$deviceId").apply()

        val current = _discoveredDevices.value.toMutableList()
        val index = current.indexOfFirst { it.deviceId == deviceId }
        if (index >= 0) {
            current[index] = current[index].copy(isPaired = false)
            _discoveredDevices.value = current
        }
    }

    private suspend fun verifyAuth() {
        val result = webSocketClient.sendCommand(PcCommand.Ping, authToken)
        if (result is PcCommandResult.PingResult) {
            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.CONNECTED
            )
        } else {
            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.PAIRING
            )
        }
    }

    private fun saveToken(deviceId: String, token: String) {
        prefs.edit().putString("token_$deviceId", token).apply()
    }

    private fun getSavedToken(deviceId: String): String? {
        return prefs.getString("token_$deviceId", null)
    }

    private fun markDevicePaired(deviceId: String) {
        val pairedIds = prefs.getStringSet("paired_devices", emptySet())?.toMutableSet()
            ?: mutableSetOf()
        pairedIds.add(deviceId)
        prefs.edit().putStringSet("paired_devices", pairedIds).apply()
    }

    private fun isDevicePaired(deviceId: String): Boolean {
        val pairedIds = prefs.getStringSet("paired_devices", emptySet()) ?: emptySet()
        return deviceId in pairedIds
    }
}
