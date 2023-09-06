package com.serial.nats.communication.core.device.manager.activity

import android.app.Activity
import com.serial.nats.communication.core.device.manager.DeviceManager
import javax.inject.Inject

class ActivityDeviceManagerFactory @Inject constructor(
    private val deviceManager: DeviceManager
) {
    fun create(activity: Activity): ActivityDeviceManager {
        return ActivityDeviceManagerImpl(
            activity = activity,
            manager = deviceManager
        )
    }
}