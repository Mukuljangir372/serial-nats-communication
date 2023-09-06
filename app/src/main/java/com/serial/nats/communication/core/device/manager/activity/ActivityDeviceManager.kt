package com.serial.nats.communication.core.device.manager.activity

import com.serial.nats.communication.core.device.manager.NativeDevice

interface ActivityDeviceManager {
    suspend fun requestDevicePermission(deviceId: String): NativeDevice
}