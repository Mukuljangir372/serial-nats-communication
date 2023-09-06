package com.serial.nats.communication.domain

import com.serial.nats.communication.core.device.manager.NativeDevice

interface DeviceRepository {
    suspend fun getDevices(): List<NativeDevice>
}