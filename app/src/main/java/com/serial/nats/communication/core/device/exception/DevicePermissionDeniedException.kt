package com.serial.nats.communication.core.device.exception

class DevicePermissionDeniedException(private val deviceId: String) : Exception() {
    override fun getLocalizedMessage(): String {
        return "Device: $deviceId permission denied"
    }
}