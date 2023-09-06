package com.serial.nats.communication.core.device.manager

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.ProlificSerialDriver
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
            val devices = usbManager.deviceList.mapNotNull { it.value }
            return convertDeviceListToNativeDeviceList(devices)
        }

        private fun getCustomUsbProber(device: UsbDevice): UsbSerialProber {
            val table = ProbeTable()
            table.addProduct(device.vendorId, device.productId, UsbSerialDriver::class.java)
            table.addProduct(device.vendorId, device.productId, FtdiSerialDriver::class.java)
            table.addProduct(device.vendorId, device.productId, CdcAcmSerialDriver::class.java)
            table.addProduct(device.vendorId, device.productId, Ch34xSerialDriver::class.java)
            table.addProduct(device.vendorId, device.productId, Cp21xxSerialDriver::class.java)
            table.addProduct(device.vendorId, device.productId, ProlificSerialDriver::class.java)
            return UsbSerialProber(table)
        }

        private fun getDeviceDriver(device: UsbDevice): UsbSerialDriver {
            val defaultSerialProber = UsbSerialProber.getDefaultProber()
            if (defaultSerialProber.probeDevice(device) != null) {
                return defaultSerialProber.probeDevice(device)
            }
            return getCustomUsbProber(device).probeDevice(device)
        }

        private fun getUsbManager(context: Context): UsbManager {
            return context.getSystemService(Context.USB_SERVICE) as UsbManager
        }

        private fun convertDeviceListToNativeDeviceList(
            deviceList: List<UsbDevice>,
        ): List<NativeDevice> {
            return deviceList.map { device ->
                val driver = getDeviceDriver(device)
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
            port: UsbSerialPort,
        ): NativeDevice {
            return NativeDevice(
                id = "${driver.device.deviceId}",
                name = driver.device.deviceName,
                port = port,
                driver = driver,
                device = driver.device,
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
//            val ioManager = SerialInputOutputManager(device.port)
//            ioManager.start()
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