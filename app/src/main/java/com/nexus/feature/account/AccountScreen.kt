package com.nexus.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PersonAddAlt1
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
import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.ui.components.NexusAvatar
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusLabeledTextField
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusStatusChip
import com.nexus.ui.components.NexusStatusTone
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(innerPadding: PaddingValues) {
    val repository = remember { AppGraph.repository }
    val useCase = remember { BindWuwaAccountUseCase(repository) }
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
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
            }

            if (accounts.isEmpty()) {
                NexusEmptyStateCard(
                    title = "还没有绑定账号",
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
                    label = "Token",
                    value = token,
                    onValueChange = { token = it },
                )
                NexusLabeledTextField(
                    label = "账号备注",
                    value = nickname,
                    onValueChange = { nickname = it },
                )
                NexusPrimaryButton(
                    label = if (isSubmitting) "添加中..." else "添加账号",
                    icon = Icons.Outlined.Add,
                    onClick = {
                        if (isSubmitting) return@NexusPrimaryButton
                        scope.launch {
                            isSubmitting = true
                            when (val result = useCase(token, nickname.ifBlank { null })) {
                                is OperationResult.Success -> {
                                    reloadAccounts()
                                    token = ""
                                    nickname = ""
                                }
                                is OperationResult.Failure -> Unit
                            }
                            isSubmitting = false
                        }
                    },
                    enabled = token.isNotBlank() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
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
