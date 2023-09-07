package com.serial.nats.communication.core.nats

interface NatsListener {
    fun onSubjectDataReceive(data: ByteArray)
}