package com.serial.nats.communication.domain

import com.serial.nats.communication.core.device.manager.NativeDevice

interface DeviceRepository {
    suspend fun getDevices(): List<NativeDevice>
    suspend fun openConnection(deviceId: String): NativeDevice
    suspend fun closeConnection(deviceId: String): NativeDevice
    suspend fun readBytes(deviceId: String): ByteArray
    suspend fun writeBytes(deviceId: String, bytes: ByteArray)
}