package com.serial.nats.communication.core.device

import android.content.Context
import android.hardware.usb.UsbManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class NativeDeviceManager(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : DeviceManager {
    override suspend fun getDevices(): List<Device> {
        return withContext(dispatcher) {
            getDevices(context)
        }
    }

    companion object {
        private fun getDevices(context: Context): List<Device> {
            val usbManager = getUsbManager(context)
            return emptyList()
        }

        private fun getUsbManager(context: Context): UsbManager {
            return context.getSystemService(Context.USB_SERVICE) as UsbManager
        }
    }
}