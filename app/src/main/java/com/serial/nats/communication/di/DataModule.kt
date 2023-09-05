package com.serial.nats.communication.di

import com.serial.nats.communication.core.device.DeviceManager
import com.serial.nats.communication.data.DeviceRepositoryImpl
import com.serial.nats.communication.domain.DeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    companion object {
        @Provides
        @Singleton
        fun provideDeviceRepository(deviceManager: DeviceManager): DeviceRepository {
            return DeviceRepositoryImpl(
                dispatcher = Dispatchers.IO,
                deviceManager = deviceManager
            )
        }
    }
}