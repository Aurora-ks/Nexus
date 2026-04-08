package com.nexus.feature.checkin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CheckInViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()
}

data class CheckInUiState(
    val progressText: String = "0 / 0",
    val lastResult: String = "暂无执行记录",
)
