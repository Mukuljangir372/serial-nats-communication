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
    val natsState by viewModel.natsState.collectAsState()

    DeviceManager(viewModel = viewModel, state = state)

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    DeviceConnectionScreenContent(
        state = state,
        connect = viewModel::connectDevice,
        disconnect = viewModel::disconnectDevice,
        readBytesFromDevice = viewModel::readBytesFromDeviceManually,
        writeBytesToDevice = viewModel::writeBytesToDeviceManually,
        natsState = natsState,
        writeBytesToNats = viewModel::writeBytesToNatsManually
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
    natsState: NatsState,
    connect: () -> Unit,
    disconnect: () -> Unit,
    readBytesFromDevice: () -> Unit,
    writeBytesToDevice: () -> Unit,
    writeBytesToNats: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DeviceList(state.devices)
            Loading(state.loading)
            Error(state.errorMessage)
            DeviceConnection(state.deviceConnected, state.connectionDevice)
            DeviceActions(
                connected = state.deviceConnected,
                connect = connect,
                disconnect = disconnect,
                readBytes = readBytesFromDevice,
                writeBytes = writeBytesToDevice
            )
            DeviceBytes(bytesRead = state.bytesRead, bytesWrite = state.bytesWrite)
            Nats(state = natsState, writeBytes = writeBytesToNats)
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
private fun DeviceBytes(bytesRead: String, bytesWrite: String) {
    Text(
        text = "Device Bytes Read: $bytesRead",
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = "Device Bytes Write: $bytesWrite",
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
            Text(text = "Read Bytes From Device")
        }
        Button(onClick = writeBytes) {
            Text(text = "Write Bytes To Device")
        }
    }
}

@Composable
private fun Nats(
    state: NatsState,
    writeBytes: () -> Unit
) {
    Text(
        text = "Nats",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.Blue
    )
    Text(text = "Nats Connected: ${state.connected}")
    Text(text = "Nats Loading: ${state.loading}")
    Text(text = "Nats Error: ${state.errorMessage}")
    Text(
        text = "Nats Bytes Write: ${state.writingBytes}",
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = "Nats Bytes Read: ${state.readingBytes}",
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
    Button(onClick = writeBytes) {
        Text(text = "Write Bytes To Nats")
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
        readBytesFromDevice = {},
        writeBytesToDevice = {},
        natsState = NatsState.idle,
        writeBytesToNats = {}
    )
}