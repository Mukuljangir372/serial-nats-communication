package com.serial.nats.communication.presentation.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serial.nats.communication.core.device.exception.DeviceNotFoundException
import com.serial.nats.communication.core.device.exception.DevicePermissionDeniedException
import com.serial.nats.communication.core.device.manager.NativeDevice
import com.serial.nats.communication.core.device.manager.activity.ActivityDeviceManagerFactory
import com.serial.nats.communication.domain.DeviceRepository
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceConnectionViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val activityDeviceManagerFactory: ActivityDeviceManagerFactory
) : ViewModel() {
    private val _state = MutableStateFlow(DeviceConnectionState.idle)
    val uiState get() = convertStateToUiStateFlow(_state, viewModelScope)

    fun load() {
        loadDevices()
    }

    fun connectDevice() {
        connectToDevice()
    }

    fun disconnectDevice() {
        disconnectFromDevice()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val modifiedState = getDevicesAsState(
                state = _state,
                repository = deviceRepository
            )
            _state.update { modifiedState.copy(loading = false) }
        }
    }

    private fun connectToDevice() {
        if (_state.value.devices.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val modifiedState = connectDeviceAsState(
                deviceId = _state.value.devices.first().id,
                state = _state,
                repository = deviceRepository
            )
            _state.update { modifiedState.copy(loading = false) }
        }
    }

    private fun disconnectFromDevice() {
        if (_state.value.devices.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val modifiedState = disconnectDeviceAsState(
                deviceId = _state.value.devices.first().id,
                state = _state,
                repository = deviceRepository
            )
            _state.update { modifiedState.copy(loading = false) }
        }
    }

    fun getActivityDeviceManagerFactory(): ActivityDeviceManagerFactory {
        return activityDeviceManagerFactory
    }

    companion object {
        private fun convertStateToUiStateFlow(
            state: StateFlow<DeviceConnectionState>,
            scope: CoroutineScope
        ): StateFlow<DeviceConnectionUiState> {
            return state
                .map { convertStateToUiState(it) }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = DeviceConnectionUiState.idle
                )
        }

        private fun convertStateToUiState(
            state: DeviceConnectionState
        ): DeviceConnectionUiState {
            val connectionDevice = if (state.connectionDevice != null) {
                convertDeviceToDisplayDevice(state.connectionDevice)
            } else null
            return DeviceConnectionUiState(
                loading = state.loading,
                devices = state.devices.map { convertDeviceToDisplayDevice(it) },
                errorMessage = state.errorMessage,
                connectionDevice = connectionDevice,
                deviceConnected = state.deviceConnected,
                deviceRequirePermission = state.deviceRequirePermission,
                devicePermissionGranted = state.devicePermissionGranted
            )
        }

        private fun convertDeviceToDisplayDevice(
            nativeDevice: NativeDevice
        ): DisplayNativeDevice {
            return DisplayNativeDevice(
                id = nativeDevice.id,
                name = nativeDevice.name
            )
        }

        private suspend fun getDevices(
            repository: DeviceRepository
        ): List<NativeDevice> {
            return repository.getDevices()
        }

        private suspend fun getDevicesAsState(
            state: StateFlow<DeviceConnectionState>,
            repository: DeviceRepository
        ): DeviceConnectionState {
            val devices = getDevices(repository)
            return state.value.copy(devices = devices, connectionDevice = devices.firstOrNull())
        }

        private suspend fun connectDevice(
            deviceId: String,
            repository: DeviceRepository
        ): NativeDevice {
            return repository.openConnection(deviceId)
        }

        private suspend fun disconnectDevice(
            deviceId: String,
            repository: DeviceRepository
        ): NativeDevice {
            return repository.closeConnection(deviceId)
        }

        private suspend fun connectDeviceAsState(
            deviceId: String,
            repository: DeviceRepository,
            state: StateFlow<DeviceConnectionState>
        ): DeviceConnectionState {
            return try {
                val device = connectDevice(deviceId, repository)
                state.value.copy(connectionDevice = device, deviceConnected = true)
            } catch (e: DeviceNotFoundException) {
                state.value.copy(errorMessage = e.localizedMessage)
            } catch (e: DevicePermissionDeniedException) {
                state.value.copy(errorMessage = e.localizedMessage, deviceRequirePermission = true)
            } catch (e: Exception) {
                state.value.copy(errorMessage = e.localizedMessage ?: e.message ?: "")
            }
        }

        private suspend fun disconnectDeviceAsState(
            deviceId: String,
            repository: DeviceRepository,
            state: StateFlow<DeviceConnectionState>
        ): DeviceConnectionState {
            return try {
                val device = disconnectDevice(deviceId, repository)
                state.value.copy(connectionDevice = device, deviceConnected = false)
            } catch (e: DeviceNotFoundException) {
                state.value.copy(errorMessage = e.localizedMessage)
            } catch (e: DevicePermissionDeniedException) {
                state.value.copy(errorMessage = e.localizedMessage, deviceRequirePermission = true)
            } catch (e: Exception) {
                state.value.copy(errorMessage = e.localizedMessage ?: e.message ?: "")
            }
        }
    }
}