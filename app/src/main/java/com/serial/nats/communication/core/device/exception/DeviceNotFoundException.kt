package com.serial.nats.communication.core.device.exception

class DeviceNotFoundException(private val deviceId: String) : Exception() {
    override fun getLocalizedMessage(): String {
        return "Device: $deviceId not found."
    }
}