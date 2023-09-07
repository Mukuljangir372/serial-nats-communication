package com.serial.nats.communication.core.nats

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.serial.nats.communication.core.nats.exception.NatsNotConnectedException
import io.nats.client.Connection
import io.nats.client.Dispatcher
import io.nats.client.Nats
import io.nats.client.Options
import java.io.FileInputStream
import java.time.Duration
import java.util.Properties

class NatsManagerImpl(
    private val config: NatsConfig
) : NatsManager {
    @SuppressLint("NewApi")
    private val options = getOptionBuilder(config).build()
    private var connection: Connection? = null
    private var listener: NatsListener? = null
    private var subjectDispatcher: Dispatcher? = null

    @Throws
    override fun connect(listener: NatsListener) {
        connection = Nats.connect(options)
        this.listener = listener
        subscribeToSubject()
    }

    @Throws
    override fun disconnect() {
        connection?.close()
        subjectDispatcher?.unsubscribe(config.subscribeToSubject)
        subjectDispatcher = null
        connection = null
        listener = null
    }

    @Throws
    override fun publish(data: ByteArray) {
        publishToSubject(
            connection = connection,
            data = data,
            publishSubject = config.publishToSubject
        )
    }

    private fun subscribeToSubject() {
        requireOpenConnection(connection)
        subjectDispatcher = createSubscriptionDispatcher(connection!!, listener)
        subjectDispatcher!!.subscribe(config.subscribeToSubject)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private fun getOptionBuilder(config: NatsConfig): Options.Builder {
            return Options.Builder()
                .userInfo(config.username, config.password)
                .connectionTimeout(Duration.ofMinutes(10))
                .maxReconnects(5)
                .server(config.url)
                .properties(getCertificateProperty())
        }

        private fun getCertificateProperty(): Properties {
            val properties = Properties()
            val rootPath = "/Users/mukuljangir/Documents/serialnatscommunication"
            val fileInputStream = FileInputStream("$rootPath/ca.pem")
            properties.load(fileInputStream)
            return properties
        }

        private fun requireOpenConnection(connection: Connection?) {
            if (connection == null || connection.status != Connection.Status.CONNECTED) {
                throw NatsNotConnectedException(connection?.status)
            }
        }

        private fun publishToSubject(
            connection: Connection?,
            data: ByteArray,
            publishSubject: String
        ) {
            requireOpenConnection(connection)
            connection!!.publish(publishSubject, data)
        }

        private fun createSubscriptionDispatcher(
            connection: Connection,
            listener: NatsListener?
        ): Dispatcher {
            return connection.createDispatcher { message ->
                listener?.onSubjectDataReceive(message.data)
            }
        }
    }
}