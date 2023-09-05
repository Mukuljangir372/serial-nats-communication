package com.serial.nats.communication.presentation

data class DeviceConnectionState(
    val loading: Boolean,
) {
    companion object {
        val idle = DeviceConnectionState(
            loading = false
        )
    }
}

data class DeviceConnectionUiState(
    val loading: Boolean
) {
    companion object {
        val idle = DeviceConnectionUiState(
            loading = false
        )
    }
}