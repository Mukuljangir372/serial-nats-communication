package com.serial.nats.communication.presentation.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serial.nats.communication.core.device.manager.NativeDevice
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
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DeviceConnectionState.idle)
    val uiState get() = convertStateToUiStateFlow(_state, viewModelScope)

    fun load() {
        loadDevices()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val modifiedState = getDevicesAsState(
                state = _state, repository = deviceRepository
            )
            _state.update { modifiedState.copy(loading = false) }
        }
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
            return DeviceConnectionUiState(loading = state.loading,
                devices = state.devices.map { convertDeviceToDisplayDevice(it) })
        }

        private fun convertDeviceToDisplayDevice(
            device: NativeDevice
        ): DisplayNativeDevice {
            return DisplayNativeDevice(
                id = device.id, name = device.name
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
            return state.value.copy(devices = devices)
        }
    }
}