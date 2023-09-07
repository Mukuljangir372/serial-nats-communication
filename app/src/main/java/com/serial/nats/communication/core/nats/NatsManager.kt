package com.serial.nats.communication.core.nats

interface NatsManager {
    fun connect(listener: NatsListener)
    fun disconnect()
    fun publish(data: ByteArray)
}