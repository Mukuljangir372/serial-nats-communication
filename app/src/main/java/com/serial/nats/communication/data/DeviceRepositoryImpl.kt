package com.serial.nats.communication.data

import com.serial.nats.communication.core.device.manager.DeviceManager
import com.serial.nats.communication.core.device.manager.NativeDevice
import com.serial.nats.communication.domain.DeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeviceRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val deviceManager: DeviceManager
) : DeviceRepository {
    override suspend fun getDevices(): List<NativeDevice> {
        return withContext(dispatcher) {
            deviceManager.getDevices()
        }
    }

    override suspend fun openConnection(
        deviceId: String
    ): NativeDevice {
        return withContext(dispatcher) {
            deviceManager.openConnection(deviceId)
        }
    }

    override suspend fun closeConnection(
        deviceId: String
    ): NativeDevice {
        return withContext(dispatcher) {
            deviceManager.closeConnection(deviceId)
        }
    }
}