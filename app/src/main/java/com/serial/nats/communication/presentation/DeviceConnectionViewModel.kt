package com.serial.nats.communication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DeviceConnectionViewModel : ViewModel() {
    private val _state = MutableStateFlow(DeviceConnectionState.idle)
    val uiState get() = convertStateToUiStateFlow(_state, viewModelScope)

    companion object {
        private fun convertStateToUiStateFlow(
            state: StateFlow<DeviceConnectionState>,
            scope: CoroutineScope
        ): StateFlow<DeviceConnectionUiState> {
            return state.map { convertStateToUiState(it) }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = DeviceConnectionUiState.idle
                )
        }

        private fun convertStateToUiState(
            state: DeviceConnectionState
        ): DeviceConnectionUiState {
            return DeviceConnectionUiState(
                loading = state.loading
            )
        }
    }
}