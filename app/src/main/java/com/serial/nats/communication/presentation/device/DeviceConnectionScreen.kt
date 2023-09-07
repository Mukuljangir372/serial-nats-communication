package com.serial.nats.communication.presentation.device

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice
import com.serial.nats.communication.presentation.device.resource.DeviceConnectionStateResource

@Composable
fun DeviceConnectionScreen() {
    val viewModel = hiltViewModel<DeviceConnectionViewModel>()
    val state by viewModel.uiState.collectAsState()

    DeviceManager(viewModel = viewModel, state = state)

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    DeviceConnectionScreenContent(
        state = state,
        connect = viewModel::connectDevice,
        disconnect = viewModel::disconnectDevice,
        readBytes = viewModel::readBytes,
        writeBytes = viewModel::writeBytes
    )
}

@Composable
private fun DeviceManager(
    viewModel: DeviceConnectionViewModel,
    state: DeviceConnectionUiState
) {
    val context = LocalContext.current
    val manager = remember(context) {
        viewModel.getActivityDeviceManagerFactory().create(context as Activity)
    }
    LaunchedEffect(state.deviceRequirePermission) {
        if (state.deviceRequirePermission && state.connectionDevice != null) {
            manager.requestDevicePermission(state.connectionDevice.id)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceConnectionScreenContent(
    state: DeviceConnectionUiState,
    connect: () -> Unit,
    disconnect: () -> Unit,
    readBytes: () -> Unit,
    writeBytes: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DeviceList(state.devices)

            Spacer(modifier = Modifier.height(12.dp))
            Loading(state.loading)

            Spacer(modifier = Modifier.height(12.dp))
            Error(state.errorMessage)

            Spacer(modifier = Modifier.height(12.dp))
            DeviceConnection(state.deviceConnected, state.connectionDevice)

            Spacer(modifier = Modifier.height(12.dp))
            DeviceActions(
                connected = state.deviceConnected,
                connect = connect,
                disconnect = disconnect,
                readBytes = readBytes,
                writeBytes = writeBytes
            )

            Spacer(modifier = Modifier.height(12.dp))
            Bytes(bytesRead = state.bytesRead, bytesWrite = state.bytesWrite)
        }
    }
}

@Composable
private fun DeviceList(devices: List<DisplayNativeDevice>) {
    Text(
        text = "Devices",
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(12.dp))
    devices.forEach { device ->
        Text(text = device.name)
    }
}

@Composable
private fun Error(message: String) {
    Text(
        text = "Error: $message",
        color = Color.Red
    )
}

@Composable
private fun DeviceConnection(connected: Boolean, device: DisplayNativeDevice?) {
    Text(
        text = "Connected: $connected (${device?.name})",
        color = Color.Green
    )
}

@Composable
private fun Loading(loading: Boolean) {
    Text(
        text = "Loading: $loading",
        color = Color.Blue
    )
}

@Composable
private fun Bytes(bytesRead: String, bytesWrite: String) {
    Text(
        text = "Bytes Read: $bytesRead",
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = "Bytes Write: $bytesWrite",
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun DeviceActions(
    connected: Boolean,
    connect: () -> Unit,
    disconnect: () -> Unit,
    readBytes: () -> Unit,
    writeBytes: () -> Unit,
) {
    if (!connected) {
        Button(onClick = connect) {
            Text(text = "Connect")
        }
    } else {
        Button(onClick = disconnect) {
            Text(text = "Disconnect")
        }
        Button(onClick = readBytes) {
            Text(text = "Read Bytes")
        }
        Button(onClick = writeBytes) {
            Text(text = "Write Bytes")
        }
    }
}

@Preview
@Composable
private fun ScreenPreview() {
    val state = DeviceConnectionStateResource.state
    DeviceConnectionScreenContent(
        state = state,
        connect = {},
        disconnect = {},
        readBytes = {},
        writeBytes = {}
    )
}