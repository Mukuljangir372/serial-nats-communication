package com.serial.nats.communication.core.nats.exception

import io.nats.client.Connection.Status

class NatsNotConnectedException(private val status: Status?) : Exception() {
    override fun getLocalizedMessage(): String {
        return "Nats not connected. Current status is ${status?.name}. You need to open the connection first."
    }
}