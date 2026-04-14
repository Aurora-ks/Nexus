package com.nexus.feature.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nexus.app.AppGraph
import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.ui.components.NexusAvatar
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusStatusChip
import com.nexus.ui.components.NexusStatusTone
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(innerPadding: PaddingValues) {
    val repository = remember { AppGraph.repository }
    val syncUseCase = remember { SyncWuwaAccountsUseCase(repository) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var accounts by remember { mutableStateOf(emptyList<WuwaAccount>()) }
    var cards by remember { mutableStateOf(emptyList<DashboardCardModel>()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("点击刷新概览后展示最新摘要。") }

    suspend fun reloadLocalState() {
        accounts = repository.getBoundAccounts()
        cards = repository.getCachedDashboardCards()
    }

    fun updateStatusMessage() {
        statusMessage = when {
            accounts.isEmpty() -> "还没有接入账号，请先去账号页完成绑定。"
            cards.isEmpty() -> "账号已绑定，点击刷新概览后会拉取最新摘要。"
            else -> "已载入最近一次同步快照。"
        }
    }

    LaunchedEffect(Unit) {
        reloadLocalState()
        updateStatusMessage()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    reloadLocalState()
                    updateStatusMessage()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val overview = remember(accounts, cards) { buildOverviewState(accounts, cards) }
    val focusAccount = overview.focusAccount
    val focusCard = overview.focusCard

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
                text = "概览",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
            NexusPrimaryButton(
                label = if (isRefreshing) "刷新中..." else "刷新概览",
                icon = Icons.Outlined.Refresh,
                onClick = {
                    if (isRefreshing) return@NexusPrimaryButton
                    scope.launch {
                        isRefreshing = true
                        when (val result = syncUseCase()) {
                            is OperationResult.Success -> {
                                cards = result.value
                                accounts = repository.getBoundAccounts()
                                statusMessage = if (result.value.isEmpty()) {
                                    "已尝试同步，但目前还没有可展示的账号摘要。"
                                } else {
                                    "同步完成，共更新 ${result.value.size} 个账号。"
                                }
                            }
                            is OperationResult.Failure -> {
                                reloadLocalState()
                                statusMessage = "同步失败，请稍后再试。"
                            }
                        }
                        isRefreshing = false
                    }
                },
                modifier = Modifier.width(132.dp),
            )

            NexusPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "今日签到状态",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Text(
                    text = overview.summaryText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NexusStatusChip(
                        text = "${overview.successCount} 已签到",
                        tone = NexusStatusTone.Success,
                    )
                    NexusStatusChip(
                        text = "${overview.pendingCount} 待处理",
                        tone = NexusStatusTone.Warning,
                    )
                    NexusStatusChip(
                        text = "${overview.alertCount} 异常",
                        tone = NexusStatusTone.Error,
                    )
                }
            }

            if (focusAccount == null) {
                NexusEmptyStateCard(
                    title = "暂无账号概览",
                    description = "完成账号绑定后，这里会展示体力、签到与周常摘要。",
                    icon = Icons.Outlined.Refresh,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                NexusPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NexusAvatar(label = focusAccount.nickname ?: focusAccount.roleName)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = focusAccount.nickname ?: focusAccount.roleName,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                            )
                            Text(
                                text = "${focusAccount.serverName} · ${focusAccount.roleName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                        NexusStatusChip(
                            text = overview.focusStatusLabel,
                            tone = overview.focusTone,
                        )
                    }
                    Text(
                        text = focusCard?.energyText ?: "结晶波片 --/--",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                    Text(
                        text = "周常进度",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextMuted,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val weeklyItems = focusCard?.weeklyFocus.orEmpty().ifEmpty {
                            listOf("周本 --/--", "危行 --/--", "深塔 --/--")
                        }
                        weeklyItems.take(3).forEach { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                    }
                    Text(
                        text = "更新于 ${focusCard?.updatedAtText ?: "待同步"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )
                }
            }

            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
            )
        }
    }
}

private data class DashboardOverviewState(
    val summaryText: String,
    val successCount: Int,
    val pendingCount: Int,
    val alertCount: Int,
    val focusAccount: WuwaAccount?,
    val focusCard: DashboardCardModel?,
    val focusStatusLabel: String,
    val focusTone: NexusStatusTone,
)

private fun buildOverviewState(
    accounts: List<WuwaAccount>,
    cards: List<DashboardCardModel>,
): DashboardOverviewState {
    val cardByIdentity = cards.associateBy { "${it.title}:${it.subtitle}" }
    val successCount = cards.count { it.signInStatus.contains("已") && !it.signInStatus.contains("失效") }
    val pendingCount = cards.count { it.signInStatus.contains("未") || it.signInStatus.contains("待") }
    val alertCount = (accounts.size - cards.size).coerceAtLeast(0) + cards.count { it.signInStatus.contains("失效") || it.signInStatus.contains("失败") }
    val focusAccount = accounts.firstOrNull()
    val focusCard = focusAccount?.let { account ->
        cardByIdentity["${account.roleName}:${account.serverName}"]
    } ?: cards.firstOrNull()

    val focusStatusLabel = when {
        focusCard == null -> "待同步"
        focusCard.signInStatus.contains("失效") || focusCard.signInStatus.contains("失败") -> "异常"
        focusCard.signInStatus.contains("已") -> "已签到"
        else -> "待处理"
    }
    val focusTone = when (focusStatusLabel) {
        "已签到" -> NexusStatusTone.Success
        "异常" -> NexusStatusTone.Error
        else -> NexusStatusTone.Warning
    }

    val summaryText = when {
        accounts.isEmpty() -> "还没有接入任何账号，先去账号页完成 Token 绑定。"
        cards.isEmpty() -> "已接入 ${accounts.size} 个账号，当前还没有可用的同步摘要。"
        else -> "${accounts.size} 个账号已接入，其中 ${successCount.coerceAtMost(accounts.size)} 个已完成签到，${alertCount.coerceAtLeast(0)} 个需要关注。"
    }

    return DashboardOverviewState(
        summaryText = summaryText,
        successCount = successCount.coerceAtLeast(0),
        pendingCount = pendingCount.coerceAtLeast(0),
        alertCount = alertCount.coerceAtLeast(0),
        focusAccount = focusAccount,
        focusCard = focusCard,
        focusStatusLabel = focusStatusLabel,
        focusTone = focusTone,
    )
}
