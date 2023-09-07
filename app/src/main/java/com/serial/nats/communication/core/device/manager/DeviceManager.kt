package com.serial.nats.communication.core.device.manager

interface DeviceManager {
    suspend fun getDevices(): List<NativeDevice>
    suspend fun getDevice(id: String): NativeDevice
    suspend fun openConnection(deviceId: String): NativeDevice
    suspend fun closeConnection(deviceId: String): NativeDevice
    suspend fun readBytes(deviceId: String): ByteArray
    suspend fun writeBytes(deviceId: String, bytes: ByteArray)
}