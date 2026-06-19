package com.repoflow.core.data.remote.pc

import com.google.gson.Gson
import com.repoflow.core.domain.model.PcCommand
import com.repoflow.core.domain.model.PcCommandResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PcWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val RECONNECT_DELAY_MS = 1000L
        private const val MAX_RECONNECT_DELAY_MS = 30000L
    }

    private var webSocket: WebSocket? = null
    private var currentUrl: String? = null
    private var reconnectAttempt = 0
    private var shouldReconnect = false
    private var onConnected: (() -> Unit)? = null
    private var onDisconnected: ((String) -> Unit)? = null

    private val pendingRequests = ConcurrentHashMap<String, Channel<PcResponseMessage>>()

    private val client: OkHttpClient = okHttpClient.newBuilder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    fun connect(
        host: String,
        port: Int,
        onConnected: () -> Unit,
        onDisconnected: (String) -> Unit
    ) {
        this.onConnected = onConnected
        this.onDisconnected = onDisconnected
        shouldReconnect = true
        reconnectAttempt = 0
        currentUrl = "ws://$host:$port/bridge"
        doConnect(currentUrl!!)
    }

    private fun doConnect(url: String) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Sec-WebSocket-Protocol", "repoflow-bridge-v1")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempt = 0
                onConnected?.invoke()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val response = gson.fromJson(text, PcResponseMessage::class.java)
                    if (response.type == "response") {
                        val requestId = response.id
                        if (requestId != null) {
                            pendingRequests[requestId]?.trySend(response)
                        }
                    }
                } catch (_: Exception) { }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onDisconnected?.invoke(reason)
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onDisconnected?.invoke(t.message ?: "Connection failed")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect) return
        val delay = minOf(
            RECONNECT_DELAY_MS * (1 shl reconnectAttempt.coerceAtMost(5)),
            MAX_RECONNECT_DELAY_MS
        )
        reconnectAttempt++
        Thread.sleep(delay)
        currentUrl?.let { doConnect(it) }
    }

    suspend fun sendCommand(command: PcCommand, authToken: String? = null): PcCommandResult {
        val request = PcMessageMapper.toRequest(command, authToken)
        val channel = Channel<PcResponseMessage>(Channel.CONFLATED)
        pendingRequests[request.id] = channel

        try {
            val json = gson.toJson(request)
            webSocket?.send(json)

            val response = channel.receive()
            return PcMessageMapper.toResult(response, command)
        } catch (e: Exception) {
            return PcCommandResult.Error("NETWORK_ERROR", e.message ?: "Network error")
        } finally {
            pendingRequests.remove(request.id)
            channel.close()
        }
    }

    fun sendMessage(json: String) {
        webSocket?.send(json)
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "Client closing")
        webSocket = null
        currentUrl = null
        pendingRequests.values.forEach { it.close() }
        pendingRequests.clear()
    }
}
