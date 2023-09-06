package com.serial.nats.communication.core.device.manager.activity

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import com.serial.nats.communication.core.device.manager.DeviceManager
import com.serial.nats.communication.core.device.manager.NativeDevice

class ActivityDeviceManagerImpl(
    private val activity: Activity,
    private val manager: DeviceManager
) : ActivityDeviceManager {
    override suspend fun requestDevicePermission(
        deviceId: String
    ): NativeDevice {
        return requestDevicePermission(
            deviceId = deviceId,
            activity = activity,
            manager = manager
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val INTENT_ACTION_GRANT_USB = "com.serial.nats.communication" + ".GRANT_USB"

        private fun getUsbManager(context: Context): UsbManager {
            return context.getSystemService(Context.USB_SERVICE) as UsbManager
        }

        private suspend fun requestDevicePermission(
            deviceId: String,
            activity: Activity,
            manager: DeviceManager
        ): NativeDevice {
            val usbManager = getUsbManager(activity)
            val device = manager.getDevice(deviceId)
            if (!usbManager.hasPermission(device.device)) {
                val intent = getPendingIntentForPermission(activity)
                usbManager.requestPermission(device.device, intent)
            }
            return device
        }

        private fun getPendingIntentForPermission(activity: Activity): PendingIntent {
            val intent = Intent(INTENT_ACTION_GRANT_USB)
            val flags = PendingIntent.FLAG_MUTABLE
            return PendingIntent.getBroadcast(activity, PERMISSION_REQUEST_CODE, intent, flags)
        }
    }
}