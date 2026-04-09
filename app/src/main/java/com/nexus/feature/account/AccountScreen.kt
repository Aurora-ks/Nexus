package com.nexus.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexus.app.AppGraph
import com.nexus.core.model.AppError
import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.ui.components.NexusDestructiveButton
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusLabeledTextField
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusSecondaryButton
import com.nexus.ui.components.NexusStatusChip
import com.nexus.ui.components.NexusStatusTone
import com.nexus.ui.theme.AccentPrimary
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.DestructivePrimary
import com.nexus.ui.theme.DestructiveSurface
import com.nexus.ui.theme.SurfaceCard
import com.nexus.ui.theme.SurfaceInput
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(innerPadding: PaddingValues) {
    val repository = remember { AppGraph.repository }
    val bindAccount = remember { BindWuwaAccountUseCase(repository) }
    val updateAccountRemark = remember { UpdateWuwaAccountRemarkUseCase(repository) }
    val deleteAccount = remember { DeleteWuwaAccountUseCase(repository) }
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSavingRemark by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf(emptyList<WuwaAccount>()) }
    var pendingDeletion by remember { mutableStateOf<WuwaAccount?>(null) }
    var pendingRemarkEdit by remember { mutableStateOf<WuwaAccount?>(null) }
    var remarkDraft by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

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
            Text(
                text = "账号",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )

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
                                enabled = !isDeleting && !isSavingRemark,
                                onEdit = {
                                    pendingRemarkEdit = account
                                    remarkDraft = account.nickname.orEmpty()
                                },
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
                    placeholder = null,
                )
                NexusLabeledTextField(
                    label = "账号备注",
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = null,
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
                                is OperationResult.Failure -> {
                                    statusMessage = result.error.toUserMessage()
                                }
                            }
                            isSubmitting = false
                        }
                    },
                    enabled = token.isNotBlank() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (statusMessage.isNotBlank()) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                )
            }
        }
    }

    pendingRemarkEdit?.let { account ->
        EditRemarkDialog(
            account = account,
            remark = remarkDraft,
            onRemarkChange = { remarkDraft = it },
            isSaving = isSavingRemark,
            onDismiss = {
                if (!isSavingRemark) {
                    pendingRemarkEdit = null
                    remarkDraft = ""
                }
            },
            onConfirm = {
                if (isSavingRemark) return@EditRemarkDialog
                scope.launch {
                    val nextRemark = remarkDraft.trim()
                    isSavingRemark = true
                    when (val result = updateAccountRemark(account.id, nextRemark.ifBlank { null })) {
                        is OperationResult.Success -> {
                            reloadAccounts()
                            pendingRemarkEdit = null
                            remarkDraft = ""
                        }
                        is OperationResult.Failure -> {
                            statusMessage = result.error.toUserMessage()
                        }
                    }
                    isSavingRemark = false
                }
            },
        )
    }

    pendingDeletion?.let { account ->
        DeleteAccountDialog(
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
                            statusMessage = "账号已删除。"
                        }
                        is OperationResult.Failure -> {
                            statusMessage = "删除失败，请稍后再试。"
                        }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = SurfaceInput,
        ) {
            Spacer(modifier = Modifier.fillMaxSize())
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = account.displayName(),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = "${account.serverName} · ${account.roleName}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
        }
        NexusStatusChip(
            text = "已连接",
            tone = NexusStatusTone.Success,
        )
        InlineIconAction(
            icon = Icons.Outlined.DeleteOutline,
            contentDescription = "删除账号",
            tint = DestructivePrimary,
            enabled = enabled,
            onClick = onDelete,
        )
        EditRemarkButton(
            enabled = enabled,
            onClick = onEdit,
        )
    }
}

@Composable
private fun EditRemarkButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = TextPrimary.copy(alpha = if (enabled) 1f else 0.6f)
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "编辑备注",
            modifier = Modifier.size(14.dp),
            tint = contentColor,
        )
    }
}

@Composable
private fun InlineIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = tint.copy(alpha = if (enabled) 1f else 0.5f),
        )
    }
}

@Composable
private fun EditRemarkDialog(
    account: WuwaAccount,
    remark: String,
    onRemarkChange: (String) -> Unit,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val normalizedRemark = remark.trim()
    val saveEnabled = !isSaving && normalizedRemark != account.nickname.orEmpty()

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
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.CenterHorizontally),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = BackgroundWarm,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Text(
                        text = "修改备注",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                        ),
                        color = TextPrimary,
                    )
                    NexusLabeledTextField(
                        label = "账号备注",
                        value = remark,
                        onValueChange = onRemarkChange,
                        placeholder = null,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        NexusSecondaryButton(
                            label = "取消",
                            onClick = onDismiss,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                        )
                        NexusPrimaryButton(
                            label = if (isSaving) "保存中..." else "保存备注",
                            icon = Icons.Outlined.Check,
                            onClick = onConfirm,
                            enabled = saveEnabled,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
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
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.CenterHorizontally),
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
                    Text(
                        text = "删除账号",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "此操作无法撤销，相关数据将被清除",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
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

private fun WuwaAccount.displayName(): String {
    return nickname?.takeIf { it.isNotBlank() } ?: roleName
}

private fun AppError.toUserMessage(): String {
    return when (this) {
        is AppError.AuthError -> message
        is AppError.ParseError -> message
        is AppError.ApiContractError -> message
        is AppError.UnknownError -> message
    }
}
