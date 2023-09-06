package com.serial.nats.communication.presentation.device

import com.serial.nats.communication.core.device.manager.NativeDevice
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice

data class DeviceConnectionState(
    val loading: Boolean,
    val errorMessage: String,
    val nativeDevices: List<NativeDevice>,
    val connectionNativeDevice: NativeDevice?,
    val deviceConnected: Boolean,
    val deviceRequirePermission: Boolean,
    val devicePermissionGranted: Boolean
) {
    companion object {
        val idle = DeviceConnectionState(
            loading = false,
            errorMessage = "",
            nativeDevices = emptyList(),
            connectionNativeDevice = null,
            deviceConnected = false,
            deviceRequirePermission = false,
            devicePermissionGranted = false
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
    val devicePermissionGranted: Boolean
) {
    companion object {
        val idle = DeviceConnectionUiState(
            loading = false,
            errorMessage = "",
            devices = emptyList(),
            connectionDevice = null,
            deviceConnected = false,
            deviceRequirePermission = false,
            devicePermissionGranted = false
        )
    }
}