package com.serial.nats.communication.di

import android.content.Context
import com.serial.nats.communication.BuildConfig
import com.serial.nats.communication.core.device.manager.DeviceManager
import com.serial.nats.communication.core.device.manager.DeviceManagerImpl
import com.serial.nats.communication.core.nats.NatsConfig
import com.serial.nats.communication.core.nats.NatsManager
import com.serial.nats.communication.core.nats.NatsManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    @Singleton
    @Binds
    fun bindDeviceManager(manager: DeviceManagerImpl): DeviceManager

    @Singleton
    @Binds
    fun bindNatsManager(managerImpl: NatsManagerImpl): NatsManager

    companion object {
        @Provides
        @Singleton
        fun provideDeviceManager(
            @ApplicationContext context: Context
        ): DeviceManagerImpl {
            return DeviceManagerImpl(
                context = context,
                dispatcher = Dispatchers.Default
            )
        }

        @Provides
        @Singleton
        fun provideNatsConfig(): NatsConfig {
            return NatsConfig(
                username = BuildConfig.NATS_USERNAME,
                password = BuildConfig.NATS_PASSWORD,
                url = BuildConfig.NATS_URL,
                publishToSubject = BuildConfig.NATS_PUBLISH_TO_SUBJECT,
                subscribeToSubject = BuildConfig.NATS_SUBSCRIBE_TO_SUBJECT
            )
        }

        @Provides
        @Singleton
        fun provideNatsManager(
            config: NatsConfig,
            @ApplicationContext context: Context
        ): NatsManagerImpl {
            return NatsManagerImpl(
                config = config,
                context = context
            )
        }
    }
}