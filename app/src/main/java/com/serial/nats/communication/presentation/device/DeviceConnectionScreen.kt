package com.serial.nats.communication.presentation.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice
import com.serial.nats.communication.presentation.device.resource.DeviceConnectionStateResource

@Composable
fun DeviceConnectionScreen() {
    val viewModel = hiltViewModel<DeviceConnectionViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    DeviceConnectionScreenContent(state = state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceConnectionScreenContent(
    state: DeviceConnectionUiState
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DeviceList(state.devices)
            Spacer(modifier = Modifier.height(12.dp))
            Error(state.errorMessage)
            Spacer(modifier = Modifier.height(12.dp))
            DeviceConnection(state.deviceConnected, state.connectionDevice)
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

@Preview
@Composable
private fun ScreenPreview() {
    val state = DeviceConnectionStateResource.state
    DeviceConnectionScreenContent(state = state)
}