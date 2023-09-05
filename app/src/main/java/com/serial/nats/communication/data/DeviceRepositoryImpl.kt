package com.serial.nats.communication.data

import com.serial.nats.communication.core.device.DeviceManager
import com.serial.nats.communication.core.device.NativeDevice
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
}