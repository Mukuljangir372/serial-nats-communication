package com.serial.nats.communication.core.device

interface DeviceManager {
    suspend fun getDevices(): List<NativeDevice>
    suspend fun openConnection(deviceId: String): NativeDevice
    suspend fun closeConnection(deviceId: String): NativeDevice
}