package com.nexus.feature.account

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()
}

data class AccountUiState(
    val token: String = "",
    val nickname: String = "",
    val isSubmitting: Boolean = false,
    val message: String? = null,
)
