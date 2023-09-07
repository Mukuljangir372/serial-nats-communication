package com.serial.nats.communication.core.nats

data class NatsConfig(
    val username: String,
    val password: String,
    val url: String,
    val publishToSubject: String,
    val subscribeToSubject: String
)