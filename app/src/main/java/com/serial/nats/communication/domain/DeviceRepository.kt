package com.serial.nats.communication.domain

import com.serial.nats.communication.core.device.NativeDevice

interface DeviceRepository {
    suspend fun getDevices(): List<NativeDevice>
}