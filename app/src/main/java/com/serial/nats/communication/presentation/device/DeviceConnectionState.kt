package com.serial.nats.communication.presentation.device

import com.serial.nats.communication.core.device.manager.NativeDevice
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice

data class DeviceConnectionState(
    val loading: Boolean,
    val devices: List<NativeDevice>
) {
    companion object {
        val idle = DeviceConnectionState(
            loading = false,
            devices = emptyList()
        )
    }
}

data class DeviceConnectionUiState(
    val loading: Boolean,
    val devices: List<DisplayNativeDevice>
) {
    companion object {
        val idle = DeviceConnectionUiState(
            loading = false,
            devices = emptyList()
        )
    }
}