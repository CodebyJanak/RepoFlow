package com.repoflow.feature.pcbridge

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.domain.model.ConnectionStatus
import com.repoflow.core.domain.model.PcDevice
import com.repoflow.core.ui.components.EmptyState
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcBridgeScreen(
    onBack: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    onNavigateToPairing: (PcDevice) -> Unit,
    onNavigateToRemote: (String) -> Unit,
    viewModel: PcBridgeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        viewModel.startDiscovery()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RepoFlowTopAppBar(
                title = "PC Bridge",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { viewModel.startDiscovery() }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Scan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ConnectionStatusCard(state) }

            if (state.connection.status == ConnectionStatus.CONNECTED ||
                state.connection.status == ConnectionStatus.PAIRED
            ) {
                item {
                    Button(
                        onClick = { onNavigateToRemote(state.connection.deviceId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Computer, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Remote Workspace")
                    }
                }
            }

            item {
                Text(
                    text = "Discovered Devices",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.isScanning) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scanning for PCs on your network...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.discoveredDevices.isEmpty() && !state.isScanning) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Wifi,
                        title = "No PCs Found",
                        message = "Make sure the RepoFlow Desktop Agent is running on your PC and both devices are on the same network.",
                        actionLabel = "Scan Again",
                        onAction = { viewModel.startDiscovery() }
                    )
                }
            }

            items(state.discoveredDevices, key = { it.deviceId }) { device ->
                DeviceCard(
                    device = device,
                    isConnected = state.connection.deviceId == device.deviceId &&
                            state.connection.status == ConnectionStatus.CONNECTED,
                    onConnect = {
                        if (device.isPaired) {
                            viewModel.connectToDevice(device)
                        } else {
                            onNavigateToPairing(device)
                        }
                    },
                    onDisconnect = { viewModel.disconnect() },
                    onForget = { viewModel.forgetDevice(device) },
                    onOpenRemote = { onNavigateToRemote(device.deviceId) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ConnectionStatusCard(state: PcBridgeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = when (state.connection.status) {
                ConnectionStatus.CONNECTED, ConnectionStatus.PAIRED ->
                    MaterialTheme.colorScheme.primaryContainer
                ConnectionStatus.CONNECTING, ConnectionStatus.PAIRING ->
                    MaterialTheme.colorScheme.tertiaryContainer
                ConnectionStatus.ERROR ->
                    MaterialTheme.colorScheme.errorContainer
                else ->
                    MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (state.connection.status) {
                    ConnectionStatus.CONNECTED, ConnectionStatus.PAIRED -> Icons.Outlined.Link
                    ConnectionStatus.CONNECTING, ConnectionStatus.PAIRING ->
                        Icons.Filled.Cable
                    ConnectionStatus.ERROR -> Icons.Filled.LinkOff
                    else -> Icons.Outlined.Link
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (state.connection.status) {
                        ConnectionStatus.CONNECTED -> "Connected to ${state.connection.deviceName}"
                        ConnectionStatus.PAIRED -> "Paired with ${state.connection.deviceName}"
                        ConnectionStatus.CONNECTING -> "Connecting..."
                        ConnectionStatus.PAIRING -> "Pairing..."
                        ConnectionStatus.DISCONNECTED -> "Not Connected"
                        ConnectionStatus.ERROR -> "Connection Error"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (state.connection.status == ConnectionStatus.ERROR && state.error != null) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceCard(
    device: PcDevice,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onForget: () -> Unit,
    onOpenRemote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = { if (!isConnected) onConnect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.deviceName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${device.host}:${device.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (device.isPaired) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = "Paired",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isConnected) {
                    Button(
                        onClick = onOpenRemote,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open")
                    }
                    Button(
                        onClick = onDisconnect,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Disconnect")
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (device.isPaired) "Connect" else "Pair")
                    }
                    if (device.isPaired) {
                        Button(
                            onClick = onForget,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Forget")
                        }
                    }
                }
            }
        }
    }
}
