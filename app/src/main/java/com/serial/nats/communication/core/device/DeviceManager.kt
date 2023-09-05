package com.serial.nats.communication.core.device

interface DeviceManager {
    suspend fun getDevices(): List<Device>
}