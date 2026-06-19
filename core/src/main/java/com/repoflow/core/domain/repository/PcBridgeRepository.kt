package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.PcCommand
import com.repoflow.core.domain.model.PcCommandResult
import com.repoflow.core.domain.model.PcConnection
import com.repoflow.core.domain.model.PcDevice
import kotlinx.coroutines.flow.Flow

interface PcBridgeRepository {

    val discoveredDevices: Flow<List<PcDevice>>
    val connectionState: Flow<PcConnection>

    suspend fun startDiscovery()
    suspend fun stopDiscovery()

    suspend fun connect(device: PcDevice): Result<PcConnection>
    suspend fun pair(device: PcDevice, pairingToken: String, deviceName: String, deviceId: String): Result<PcConnection>
    suspend fun disconnect()

    suspend fun executeCommand(command: PcCommand): Result<PcCommandResult>

    suspend fun getPairedDevices(): List<PcDevice>
    suspend fun forgetDevice(deviceId: String)
}
