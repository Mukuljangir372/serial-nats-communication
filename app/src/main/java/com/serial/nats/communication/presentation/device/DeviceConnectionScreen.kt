package com.serial.nats.communication.presentation.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.serial.nats.communication.presentation.device.resource.DeviceConnectionStateResource

@Composable
fun DeviceConnectionScreen() {
    val viewModel = hiltViewModel<DeviceConnectionViewModel>()
    val state by viewModel.uiState.collectAsState()
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
            state.devices.forEach { device ->
                Text(text = device.name)
            }
        }
    }
}

@Preview
@Composable
private fun ScreenPreview() {
    val state = DeviceConnectionStateResource.state
    DeviceConnectionScreenContent(state = state)
}