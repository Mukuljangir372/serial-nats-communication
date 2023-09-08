package com.serial.nats.communication.core.nats

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.serial.nats.communication.R
import com.serial.nats.communication.core.nats.exception.NatsNotConnectedException
import io.nats.client.Connection
import io.nats.client.Dispatcher
import io.nats.client.Nats
import io.nats.client.Options
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class NatsManagerImpl(
    private val config: NatsConfig,
    context: Context
) : NatsManager {
    @SuppressLint("NewApi")
    private val options = getOptionBuilder(config, context).build()
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
        private const val CERTIFICATE_KEY = "X.509"
        private const val CERTIFICATE_NAME = "caCert"
        private const val TLS = "TLS"
        private const val CERTIFICATE_FILE = "ca.pem"

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getOptionBuilder(config: NatsConfig, context: Context): Options.Builder {
            return Options.Builder()
                .userInfo(config.username, config.password)
                .connectionTimeout(Duration.ofMinutes(10))
                .maxReconnects(5)
                .server(config.url)
                .sslContext(getSslContext(context))
        }

        private fun getSslContext(context: Context): SSLContext {
            val caCertPath = getCertificateFile(context).path

            val certificateFactory = CertificateFactory.getInstance(CERTIFICATE_KEY)
            val caCertFile = FileInputStream(caCertPath)
            val caCert: X509Certificate =
                certificateFactory.generateCertificate(caCertFile) as X509Certificate

            val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
            trustStore.load(null, null)

            trustStore.setCertificateEntry(CERTIFICATE_NAME, caCert)

            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(trustStore)

            val sslContext = SSLContext.getInstance(TLS)
            sslContext.init(null, trustManagerFactory.trustManagers, null)
            return sslContext
        }

        private fun getCertificateFile(context: Context): File {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.ca)
            val destinationFile = File("${context.cacheDir}/$CERTIFICATE_FILE")
            if (destinationFile.exists()) destinationFile.delete()
            destinationFile.createNewFile()

            val outputStream = FileOutputStream(destinationFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream.close()
            return destinationFile
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