package com.serial.nats.communication.core.device.manager

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.serial.nats.communication.core.device.exception.DeviceNotFoundException
import com.serial.nats.communication.core.device.exception.DevicePermissionDeniedException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeviceManagerImpl(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : DeviceManager {
    override suspend fun getDevices(): List<NativeDevice> {
        return withContext(dispatcher) {
            getDevices(context)
        }
    }

    override suspend fun getDevice(id: String): NativeDevice {
        return withContext(dispatcher) {
            getDeviceById(context, id)
        }
    }

    override suspend fun openConnection(
        deviceId: String
    ): NativeDevice {
        return withContext(dispatcher) {
            openDeviceConnection(context, deviceId)
        }
    }

    override suspend fun closeConnection(
        deviceId: String
    ): NativeDevice {
        return withContext(dispatcher) {
            closeDeviceConnection(context, deviceId)
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
                        port = port,
                        device = device
                    )
                }
            }.flatten()
        }

        private fun convertDriverToNativeDevice(
            driver: UsbSerialDriver,
            port: UsbSerialPort,
            device: UsbDevice
        ): NativeDevice {
            return NativeDevice(
                id = "${driver.device.deviceId}",
                name = driver.device.deviceName,
                port = port,
                driver = driver,
                device = device,
                connected = port.isOpen
            )
        }

        private fun getDeviceById(
            context: Context,
            deviceId: String
        ): NativeDevice {
            val device = getDevices(context).find { it.id == deviceId }
            if (device == null) throw DeviceNotFoundException(deviceId)
            else return device
        }

        private fun requireDevicePermission(
            usbManager: UsbManager,
            device: UsbDevice
        ) {
            if (!usbManager.hasPermission(device)) {
                throw DevicePermissionDeniedException(device.deviceId.toString())
            }
        }

        private fun openDeviceConnection(
            context: Context,
            deviceId: String
        ): NativeDevice {
            val device = getDeviceById(context, deviceId)
            val usbManager = getUsbManager(context)
            requireDevicePermission(usbManager, device.device)
            val usbConnection = usbManager.openDevice(device.device)
            device.port.open(usbConnection)
            return device.copy(connected = true)
        }

        private fun closeDeviceConnection(
            context: Context,
            deviceId: String
        ): NativeDevice {
            val device = getDeviceById(context, deviceId)
            val usbManager = getUsbManager(context)
            requireDevicePermission(usbManager, device.device)
            device.port.close()
            return device.copy(connected = false)
        }
    }
}