package com.nexus.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexus.app.AppGraph
import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.ui.components.NexusAvatar
import com.nexus.ui.components.NexusDestructiveButton
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusLabeledTextField
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusSecondaryButton
import com.nexus.ui.components.NexusStatusChip
import com.nexus.ui.components.NexusStatusTone
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.DestructivePrimary
import com.nexus.ui.theme.DestructiveSurface
import com.nexus.ui.theme.SurfaceCard
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(innerPadding: PaddingValues) {
    val repository = remember { AppGraph.repository }
    val bindAccount = remember { BindWuwaAccountUseCase(repository) }
    val deleteAccount = remember { DeleteWuwaAccountUseCase(repository) }
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf(emptyList<WuwaAccount>()) }
    var pendingDeletion by remember { mutableStateOf<WuwaAccount?>(null) }

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
                            BoundAccountRow(
                                account = account,
                                enabled = !isDeleting,
                                onDelete = { pendingDeletion = account },
                            )
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
                            when (val result = bindAccount(token, nickname.ifBlank { null })) {
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

    pendingDeletion?.let { account ->
        DeleteAccountDialog(
            account = account,
            isDeleting = isDeleting,
            onDismiss = {
                if (!isDeleting) {
                    pendingDeletion = null
                }
            },
            onConfirm = {
                if (isDeleting) return@DeleteAccountDialog
                scope.launch {
                    isDeleting = true
                    when (deleteAccount(account.id)) {
                        is OperationResult.Success -> {
                            reloadAccounts()
                            pendingDeletion = null
                        }
                        is OperationResult.Failure -> Unit
                    }
                    isDeleting = false
                }
            },
        )
    }
}

@Composable
private fun BoundAccountRow(
    account: WuwaAccount,
    enabled: Boolean,
    onDelete: () -> Unit,
) {
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
        IconButton(
            onClick = onDelete,
            enabled = enabled,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "删除账号",
                tint = DestructivePrimary,
            )
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    account: WuwaAccount,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TextPrimary.copy(alpha = 0.5f))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = SurfaceCard,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = DestructiveSurface,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = DestructivePrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "删除账号",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Text(
                            text = account.nickname ?: account.roleName,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                        Text(
                            text = "此操作无法撤销，有相关数据将被清除。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        NexusSecondaryButton(
                            label = "取消",
                            onClick = onDismiss,
                            enabled = !isDeleting,
                            modifier = Modifier.weight(1f),
                        )
                        NexusDestructiveButton(
                            label = if (isDeleting) "删除中..." else "确认删除",
                            icon = Icons.Outlined.DeleteOutline,
                            onClick = onConfirm,
                            enabled = !isDeleting,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
