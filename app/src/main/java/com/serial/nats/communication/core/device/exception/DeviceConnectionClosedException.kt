package com.serial.nats.communication.core.device.exception

class DeviceConnectionClosedException(private val deviceId: String) : Exception() {
    override fun getLocalizedMessage(): String {
        return "Device: $deviceId connection is closed. You need to open the connection on port."
    }
}