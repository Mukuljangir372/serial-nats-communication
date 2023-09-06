package com.serial.nats.communication.core.device.manager

import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort

data class NativeDevice(
    val id: String,
    val name: String,
    val port: UsbSerialPort,
    val driver: UsbSerialDriver,
    val device: UsbDevice,
    val connected: Boolean
)