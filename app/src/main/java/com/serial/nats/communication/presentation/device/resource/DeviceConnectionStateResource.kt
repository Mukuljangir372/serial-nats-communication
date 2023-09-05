package com.serial.nats.communication.presentation.device.resource

import com.serial.nats.communication.presentation.device.DeviceConnectionUiState
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice

object DeviceConnectionStateResource {
    val state = DeviceConnectionUiState(
        loading = false,
        devices = listOf(
            DisplayNativeDevice(
                id = "router-rc-1",
                name = "Router-RC-1"
            ),
            DisplayNativeDevice(
                id = "router-rc-2",
                name = "Router-RC-2"
            ),
            DisplayNativeDevice(
                id = "router-rc-3",
                name = "Router-RC-3"
            )
        )
    )
}