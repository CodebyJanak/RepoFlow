package com.repoflow.core.data.remote.pc

import android.content.Context
import android.net.wifi.WifiManager
import com.google.gson.Gson
import com.repoflow.core.domain.model.PcDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PcDiscoveryService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val DISCOVERY_PORT = 41927
        private const val BUFFER_SIZE = 512
        private const val BEACON_TIMEOUT_MS = 3000
    }

    private val _devices = MutableStateFlow<List<PcDevice>>(emptyList())
    val devices: Flow<List<PcDevice>> = _devices.asStateFlow()

    private var isListening = false
    private var listenSocket: DatagramSocket? = null
    private var listenThread: Thread? = null

    fun startListening(): Flow<PcDevice> = callbackFlow {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("pc_discovery_lock")
        multicastLock?.setReferenceCounted(true)
        multicastLock?.acquire()

        isListening = true

        val thread = Thread.ofVirtual().start {
            try {
                val socket = DatagramSocket(DISCOVERY_PORT).also { listenSocket = it }
                socket.soTimeout = BEACON_TIMEOUT_MS
                socket.reuseAddress = true

                val buffer = ByteArray(BUFFER_SIZE)

                while (isListening) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)

                        val json = String(packet.data, 0, packet.length)
                        val beacon = try {
                            gson.fromJson(json, PcBeaconMessage::class.java)
                        } catch (e: Exception) { null }

                        if (beacon != null && beacon.type == "beacon") {
                            val device = PcMessageMapper.toDevice(beacon, packet.address.hostAddress ?: "")
                            trySend(device)
                        }
                    } catch (e: SocketTimeoutException) {
                        // expected, continue looping
                    } catch (e: Exception) {
                        if (isListening) {
                            trySend(PcDevice("", "", "", 0, "", false))
                        }
                    }
                }
            } catch (_: Exception) { }
        }.also { listenThread = it }

        awaitClose {
            stopListening()
            multicastLock?.release()
        }
    }

    suspend fun sendProbe(clientName: String, protocolVersion: String = "1.0.0") = withContext(Dispatchers.IO) {
        try {
            val probe = PcProbeMessage(
                clientName = clientName,
                protocolVersion = protocolVersion
            )
            val json = gson.toJson(probe)
            val data = json.toByteArray()

            val socket = DatagramSocket()
            socket.broadcast = true

            val broadcastAddr = getBroadcastAddress()
            val packet = DatagramPacket(data, data.size, broadcastAddr, DISCOVERY_PORT)
            socket.send(packet)
            socket.close()
        } catch (_: Exception) { }
    }

    fun stopListening() {
        isListening = false
        listenSocket?.close()
        listenSocket = null
        listenThread?.interrupt()
        listenThread = null
    }

    fun updateDevices(devices: List<PcDevice>) {
        _devices.value = devices
    }

    private fun getBroadcastAddress(): InetAddress {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo = wifiManager.dhcpInfo
        val broadcast = (dhcpInfo.ipAddress and dhcpInfo.netmask) or (dhcpInfo.netmask.inv())
        val addressBytes = byteArrayOf(
            (broadcast shr 0).toByte(),
            (broadcast shr 8).toByte(),
            (broadcast shr 16).toByte(),
            (broadcast shr 24).toByte()
        )
        return InetAddress.getByAddress(addressBytes)
    }
}
