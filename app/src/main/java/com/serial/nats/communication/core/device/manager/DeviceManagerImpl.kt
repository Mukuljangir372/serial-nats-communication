package com.serial.nats.communication.core.device.manager

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
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
    private val cacheSerialPorts by lazy {
        hashMapOf<String, UsbSerialPort>()
    }

    override suspend fun getDevices(): List<NativeDevice> {
        return withContext(dispatcher) {
            getDevices(context, cacheSerialPorts)
        }
    }

    override suspend fun getDevice(id: String): NativeDevice {
        return withContext(dispatcher) {
            getDeviceById(context, id, cacheSerialPorts)
        }
    }

    override suspend fun openConnection(
        deviceId: String
    ): NativeDevice {
        return withContext(dispatcher) {
            openDeviceConnection(context, deviceId, cacheSerialPorts)
        }
    }

    override suspend fun closeConnection(
        deviceId: String
    ): NativeDevice {
        return withContext(dispatcher) {
            closeDeviceConnection(context, deviceId, cacheSerialPorts)
        }
    }

    override suspend fun readBytes(deviceId: String): ByteArray {
        return withContext(dispatcher) {
            readBytes(deviceId, context, cacheSerialPorts)
        }
    }

    override suspend fun writeBytes(deviceId: String, bytes: ByteArray) {
        return withContext(dispatcher) {
            writeBytes(deviceId, bytes, context, cacheSerialPorts)
        }
    }

    companion object {
        private const val IO_TIMEOUT_MILLIS = 2000

        private fun getDevices(
            context: Context,
            cacheSerialPorts: HashMap<String, UsbSerialPort>
        ): List<NativeDevice> {
            val usbManager = getUsbManager(context)
            val devices = usbManager.deviceList.mapNotNull { it.value }
            return convertDeviceListToNativeDeviceList(
                deviceList = devices,
                cacheSerialPorts = cacheSerialPorts
            )
        }

        private fun getCustomUsbProber(device: UsbDevice): UsbSerialProber {
            val table = ProbeTable()
            table.addProduct(device.vendorId, device.productId, FtdiSerialDriver::class.java)
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
            cacheSerialPorts: HashMap<String, UsbSerialPort>
        ): List<NativeDevice> {
            return deviceList.map { device ->
                val driver = getDeviceDriver(device)
                driver.ports.map { port ->
                    val cachePort = cacheSerialPorts["${driver.device.deviceId}+${port.portNumber}"]
                    if (cachePort == null) cacheSerialPorts["${driver.device.deviceId}+${port.portNumber}"] =
                        port
                    convertDriverToNativeDevice(
                        driver = driver,
                        port = cachePort ?: port
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
            deviceId: String,
            cacheSerialPorts: HashMap<String, UsbSerialPort>
        ): NativeDevice {
            val device = getDevices(context, cacheSerialPorts).find { it.id == deviceId }
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
            deviceId: String,
            cacheSerialPorts: HashMap<String, UsbSerialPort>
        ): NativeDevice {
            val device = getDeviceById(context, deviceId, cacheSerialPorts)
            val usbManager = getUsbManager(context)
            requireDevicePermission(usbManager, device.device)
            val usbConnection = usbManager.openDevice(device.device)
            device.port.open(usbConnection)
            return device.copy(connected = true)
        }

        private fun closeDeviceConnection(
            context: Context,
            deviceId: String,
            cacheSerialPorts: HashMap<String, UsbSerialPort>
        ): NativeDevice {
            val device = getDeviceById(context, deviceId, cacheSerialPorts)
            val usbManager = getUsbManager(context)
            requireDevicePermission(usbManager, device.device)
            device.port.close()
            return device.copy(connected = false)
        }

        private fun readBytes(
            deviceId: String,
            context: Context,
            cacheSerialPorts: HashMap<String, UsbSerialPort>
        ): ByteArray {
            val device = getDeviceById(context, deviceId, cacheSerialPorts)
            if (!device.port.isOpen) openDeviceConnection(context, deviceId, cacheSerialPorts)
            val bytes = ByteArray(8192)
            val length = device.port.read(bytes, IO_TIMEOUT_MILLIS)
            return bytes.copyOf(length)
        }

        private fun writeBytes(
            deviceId: String,
            bytes: ByteArray,
            context: Context,
            cacheSerialPorts: HashMap<String, UsbSerialPort>
        ) {
            val device = getDeviceById(context, deviceId, cacheSerialPorts)
            if (!device.port.isOpen) openDeviceConnection(context, deviceId, cacheSerialPorts)
            device.port.write(bytes, IO_TIMEOUT_MILLIS)
        }
    }
}