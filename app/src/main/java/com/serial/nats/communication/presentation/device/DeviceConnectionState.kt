package com.serial.nats.communication.presentation.device

import com.serial.nats.communication.core.device.manager.NativeDevice
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice

data class DeviceConnectionState(
    val loading: Boolean,
    val errorMessage: String,
    val devices: List<NativeDevice>,
    val connectionDevice: NativeDevice?,
    val deviceConnected: Boolean,
    val deviceRequirePermission: Boolean,
    val devicePermissionGranted: Boolean,
    val bytesRead: String,
    val bytesWrite: String,
) {
    companion object {
        val idle = DeviceConnectionState(
            loading = false,
            errorMessage = "",
            devices = emptyList(),
            connectionDevice = null,
            deviceConnected = false,
            deviceRequirePermission = false,
            devicePermissionGranted = false,
            bytesRead = "",
            bytesWrite = ""
        )
    }
}

data class DeviceConnectionUiState(
    val loading: Boolean,
    val errorMessage: String,
    val devices: List<DisplayNativeDevice>,
    val connectionDevice: DisplayNativeDevice?,
    val deviceConnected: Boolean,
    val deviceRequirePermission: Boolean,
    val devicePermissionGranted: Boolean,
    val bytesRead: String,
    val bytesWrite: String,
) {
    companion object {
        val idle = DeviceConnectionUiState(
            loading = false,
            errorMessage = "",
            devices = emptyList(),
            connectionDevice = null,
            deviceConnected = false,
            deviceRequirePermission = false,
            devicePermissionGranted = false,
            bytesRead = "",
            bytesWrite = ""
        )
    }
}