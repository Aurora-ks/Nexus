package com.nexus.feature.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.core.model.AppError
import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.WuwaRepositoryImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckInViewModel(
    private val repository: WuwaRepositoryImpl,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    fun loadCheckInStatus() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val accounts = repository.getBoundAccounts()

            if (accounts.isEmpty()) {
                _uiState.value = CheckInUiState()
                return@launch
            }

            val items = accounts.map { account ->
                async {
                    when (val result = repository.getCheckInInfo(account.id)) {
                        is OperationResult.Success -> {
                            val info = result.value
                            CheckInAccountUiState(
                                accountId = account.id,
                                roleName = account.nickname ?: info.roleName,
                                gameId = account.gameId,
                                serverName = info.serverName,
                                uidText = "UID ${account.roleId}",
                                headPhotoUrl = account.headPhotoUrl,
                                isSignedIn = info.isSignedIn,
                                signedDays = info.signedDays,
                            )
                        }

                        is OperationResult.Failure -> {
                            CheckInAccountUiState(
                                accountId = account.id,
                                roleName = account.nickname ?: account.roleName,
                                gameId = account.gameId,
                                serverName = account.serverName,
                                uidText = "UID ${account.roleId}",
                                headPhotoUrl = account.headPhotoUrl,
                                isSignedIn = false,
                                signedDays = 0,
                                errorMessage = result.error.toMessage(),
                            )
                        }
                    }
                }
            }.map { it.await() }

            _uiState.value = CheckInUiState(
                isLoading = false,
                accounts = items,
                progressText = "${items.count { it.isSignedIn }} / ${items.size}",
                lastResult = items.lastOrNull()?.statusText ?: "暂无执行记录",
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false) }
            loadCheckInStatus()
        }
    }

    fun checkIn(accountId: Long) {
        val account = _uiState.value.accounts.firstOrNull { it.accountId == accountId } ?: return
        if (account.isSignedIn || account.isSigningIn) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    accounts = state.accounts.map { item ->
                        if (item.accountId == accountId) {
                            item.copy(isSigningIn = true, errorMessage = null)
                        } else {
                            item
                        }
                    },
                )
            }

            when (val result = repository.checkIn(accountId)) {
                is OperationResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            accounts = state.accounts.map { item ->
                                if (item.accountId == accountId) {
                                    item.copy(isSigningIn = false, isSignedIn = true)
                                } else {
                                    item
                                }
                            },
                        )
                    }
                    loadCheckInStatus()
                }

                is OperationResult.Failure -> {
                    val message = result.error.toMessage()
                    _uiState.update { state ->
                        state.copy(
                            accounts = state.accounts.map { item ->
                                if (item.accountId == accountId) {
                                    item.copy(isSigningIn = false, errorMessage = message)
                                } else {
                                    item
                                }
                            },
                            lastResult = message,
                        )
                    }
                }
            }
        }
    }

    private fun AppError.toMessage(): String {
        return when (this) {
            is AppError.AuthError -> message
            is AppError.ParseError -> message
            is AppError.ApiContractError -> message
            is AppError.UnknownError -> message
        }
    }
}

data class CheckInUiState(
    val isLoading: Boolean = false,
    val accounts: List<CheckInAccountUiState> = emptyList(),
    val errorMessage: String? = null,
    val progressText: String = "0 / 0",
    val lastResult: String = "暂无执行记录",
)

data class CheckInAccountUiState(
    val accountId: Long,
    val roleName: String,
    val gameId: Int,
    val serverName: String,
    val uidText: String,
    val headPhotoUrl: String?,
    val isSignedIn: Boolean,
    val signedDays: Int,
    val isSigningIn: Boolean = false,
    val errorMessage: String? = null,
) {
    val statusText: String
        get() = when {
            errorMessage != null -> "获取失败"
            isSignedIn -> "已签到"
            else -> "未签到"
        }
}
