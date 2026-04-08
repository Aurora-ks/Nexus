package com.nexus.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.app.AppGraph
import com.nexus.core.model.AppError
import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.TokenParser
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.ui.components.NexusAvatar
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusLabeledTextField
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusSecondaryButton
import com.nexus.ui.components.NexusStatusChip
import com.nexus.ui.components.NexusStatusTone
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(innerPadding: PaddingValues) {
    val repository = remember { AppGraph.repository }
    val useCase = remember { BindWuwaAccountUseCase(repository) }
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var accounts by remember { mutableStateOf(emptyList<WuwaAccount>()) }

    suspend fun reloadAccounts() {
        accounts = repository.getBoundAccounts()
    }

    LaunchedEffect(Unit) {
        reloadAccounts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWarm)
            .statusBarsPadding()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        NexusPage {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "账号",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
                Text(
                    text = "统一管理角色绑定、Token 校验与同步策略。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            }

            if (accounts.isEmpty()) {
                NexusEmptyStateCard(
                    title = "还没有绑定账号",
                    description = "先校验 Token，再保存账号，后续签到和概览都会基于这些角色展开。",
                    icon = Icons.Outlined.PersonAddAlt1,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                NexusPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "当前绑定",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        accounts.forEach { account ->
                            BoundAccountRow(account = account)
                        }
                    }
                }
            }

            NexusPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "新增账号",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                NexusLabeledTextField(
                    label = "角色昵称",
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "例如：今汐",
                )
                NexusLabeledTextField(
                    label = "Cookie / Token",
                    value = token,
                    onValueChange = { token = it },
                    placeholder = "粘贴令牌后可先做有效性校验",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NexusSecondaryButton(
                        label = "校验 Token",
                        icon = Icons.Outlined.Shield,
                        onClick = {
                            statusMessage = when (val result = TokenParser.parseUserId(token)) {
                                is OperationResult.Success -> "Token 结构正常，识别到 userId：${result.value}"
                                is OperationResult.Failure -> errorMessage(result.error)
                            }
                        },
                        enabled = token.isNotBlank() && !isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                    NexusPrimaryButton(
                        label = if (isSubmitting) "保存中..." else "保存账号",
                        icon = Icons.Outlined.Key,
                        onClick = {
                            if (isSubmitting) return@NexusPrimaryButton
                            scope.launch {
                                isSubmitting = true
                                statusMessage = null
                                when (val result = useCase(token, nickname.ifBlank { null })) {
                                    is OperationResult.Success -> {
                                        reloadAccounts()
                                        token = ""
                                        nickname = ""
                                        statusMessage = "绑定成功，已保存 ${result.value.roleName} · ${result.value.serverName}"
                                    }
                                    is OperationResult.Failure -> {
                                        statusMessage = errorMessage(result.error)
                                    }
                                }
                                isSubmitting = false
                            }
                        },
                        enabled = token.isNotBlank() && !isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (isSubmitting) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                statusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoundAccountRow(account: WuwaAccount) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NexusAvatar(label = account.nickname ?: account.roleName)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = account.nickname ?: account.roleName,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = "${account.serverName} · ${account.roleName}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
        NexusStatusChip(
            text = "已连接",
            tone = NexusStatusTone.Success,
        )
    }
}

private fun errorMessage(error: AppError): String {
    return when (error) {
        is AppError.AuthError -> error.message
        is AppError.ParseError -> error.message
        is AppError.ApiContractError -> error.message
        is AppError.UnknownError -> error.message
    }
}
