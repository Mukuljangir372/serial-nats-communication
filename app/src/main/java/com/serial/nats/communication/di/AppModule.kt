package com.serial.nats.communication.di

import android.content.Context
import com.serial.nats.communication.core.device.manager.DeviceManager
import com.serial.nats.communication.core.device.manager.DeviceManagerImpl
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

    companion object {
        @Provides
        @Singleton
        fun provideDeviceManager(
            @ApplicationContext context: Context
        ): DeviceManagerImpl {
            return DeviceManagerImpl(
                context = context, dispatcher = Dispatchers.Default
            )
        }
    }
}