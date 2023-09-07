package com.serial.nats.communication.presentation.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serial.nats.communication.core.device.exception.DeviceNotFoundException
import com.serial.nats.communication.core.device.exception.DevicePermissionDeniedException
import com.serial.nats.communication.core.device.manager.NativeDevice
import com.serial.nats.communication.core.device.manager.activity.ActivityDeviceManagerFactory
import com.serial.nats.communication.core.nats.NatsListener
import com.serial.nats.communication.core.nats.NatsManager
import com.serial.nats.communication.domain.DeviceRepository
import com.serial.nats.communication.presentation.device.model.DisplayNativeDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DeviceConnectionViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val activityDeviceManagerFactory: ActivityDeviceManagerFactory,
    private val natsManager: NatsManager
) : ViewModel() {
    private val _state = MutableStateFlow(DeviceConnectionState.idle)
    val uiState get() = convertStateToUiStateFlow(_state, viewModelScope)

    private val _natsState = MutableStateFlow(NatsState.idle)
    val natsState get() = _natsState.asStateFlow()

    fun load() {
        loadDevices()
        connectToNats()
    }

    fun connectDevice() {
        connectToDevice()
    }

    fun disconnectDevice() {
        disconnectFromDevice()
    }

    fun readBytesFromDeviceManually() {
        readBytesFromDevice()
    }

    fun writeBytesToDeviceManually() {
        writeBytesToDevice()
    }

    fun writeBytesToNatsManually() {
        writeBytesToNats()
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

    private fun readBytesFromDevice() {
        if (_state.value.devices.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val modifiedState = readBytesAsState(
                deviceId = _state.value.devices.first().id,
                state = _state,
                repository = deviceRepository
            )
            _state.update { modifiedState.copy(loading = false) }
        }
    }

    private fun writeBytesToDevice() {
        if (_state.value.devices.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val device = _state.value.devices.first()
            val modifiedState = writeBytesAsState(
                deviceId = device.id,
                state = _state,
                repository = deviceRepository,
                byteArray = device.name.toByteArray()
            )
            _state.update { modifiedState.copy(loading = false) }
        }
    }

    private fun connectToNats() {
        viewModelScope.launch {
            _natsState.update { it.copy(loading = true) }
            val modifiedState = connectToNatsAsState(
                manager = natsManager,
                state = natsState,
                listener = object : NatsListener {
                    override fun onSubjectDataReceive(data: ByteArray) {
                        _natsState.update { it.copy(readingBytes = data.toString()) }
                    }
                },
                dispatcher = Dispatchers.Default //should be inject
            )
            _natsState.update { modifiedState.copy(loading = false) }
        }
    }

    private fun writeBytesToNats() {
        viewModelScope.launch {
            _natsState.update { it.copy(loading = true) }
            val modifiedState = writeBytesToNats(
                manager = natsManager,
                state = natsState,
                byteArray = _state.value.bytesRead.toByteArray(),
                dispatcher = Dispatchers.Default //should be inject
            )
            _natsState.update { modifiedState.copy(loading = false) }
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
                devicePermissionGranted = state.devicePermissionGranted,
                bytesRead = state.bytesRead,
                bytesWrite = state.bytesWrite
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

        private suspend fun readBytes(
            deviceId: String,
            repository: DeviceRepository
        ): ByteArray {
            return repository.readBytes(deviceId)
        }

        private suspend fun writeBytes(
            deviceId: String,
            repository: DeviceRepository,
            byteArray: ByteArray
        ) {
            repository.writeBytes(
                deviceId = deviceId,
                bytes = byteArray
            )
        }

        private suspend fun readBytesAsState(
            deviceId: String,
            repository: DeviceRepository,
            state: StateFlow<DeviceConnectionState>
        ): DeviceConnectionState {
            return try {
                val bytes = readBytes(deviceId, repository)
                state.value.copy(bytesRead = bytes.toString())
            } catch (e: Exception) {
                state.value.copy(errorMessage = e.localizedMessage ?: e.message ?: "")
            }
        }

        private suspend fun writeBytesAsState(
            deviceId: String,
            repository: DeviceRepository,
            state: StateFlow<DeviceConnectionState>,
            byteArray: ByteArray
        ): DeviceConnectionState {
            return try {
                writeBytes(deviceId, repository, byteArray)
                state.value.copy(bytesWrite = byteArray.toString())
            } catch (e: Exception) {
                state.value.copy(errorMessage = e.localizedMessage ?: e.message ?: "")
            }
        }

        private suspend fun connectToNatsAsState(
            manager: NatsManager,
            state: StateFlow<NatsState>,
            listener: NatsListener,
            dispatcher: CoroutineDispatcher
        ): NatsState {
            return withContext(dispatcher) {
                try {
                    manager.connect(listener)
                    state.value.copy(connected = true)
                } catch (e: Exception) {
                    state.value.copy(errorMessage = e.localizedMessage ?: e.message ?: "")
                }
            }
        }

        private suspend fun writeBytesToNats(
            manager: NatsManager,
            state: StateFlow<NatsState>,
            byteArray: ByteArray,
            dispatcher: CoroutineDispatcher
        ): NatsState {
            return withContext(dispatcher) {
                try {
                    manager.publish(byteArray)
                    state.value.copy(writingBytes = byteArray.toString())
                } catch (e: Exception) {
                    state.value.copy(errorMessage = e.localizedMessage ?: e.message ?: "")
                }
            }
        }
    }
}