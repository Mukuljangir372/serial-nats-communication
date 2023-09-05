package com.serial.nats.communication.core.device

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class NativeDeviceManager(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : DeviceManager {
    override suspend fun getDevices(): List<NativeDevice> {
        return withContext(dispatcher) {
            getDevices(context)
        }
    }

    companion object {
        private fun getDevices(context: Context): List<NativeDevice> {
            val usbManager = getUsbManager(context)
            val prober = UsbSerialProber.getDefaultProber()
            val devices = usbManager.deviceList.map { it.value }
            return convertDeviceListToNativeDeviceList(prober, devices)
        }

        private fun getUsbManager(context: Context): UsbManager {
            return context.getSystemService(Context.USB_SERVICE) as UsbManager
        }

        private fun convertDeviceListToNativeDeviceList(
            prober: UsbSerialProber,
            deviceList: List<UsbDevice>,
        ): List<NativeDevice> {
            return deviceList.map { device ->
                val driver = prober.probeDevice(device)
                driver.ports.map { port ->
                    convertDriverToNativeDevice(
                        driver = driver,
                        port = port
                    )
                }
            }.flatten()
        }

        private fun convertDriverToNativeDevice(
            driver: UsbSerialDriver,
            port: UsbSerialPort
        ): NativeDevice {
            return NativeDevice(
                id = "${driver.device.deviceId}-${port.portNumber}",
                name = driver.device.deviceName,
                port = port,
                driver = driver
            )
        }
    }
}