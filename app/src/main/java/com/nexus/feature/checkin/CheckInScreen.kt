package com.nexus.feature.checkin

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexus.app.AppGraph
import com.nexus.core.model.GameType
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusSecondaryButton
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.ErrorBackground
import com.nexus.ui.theme.ErrorForeground
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import com.nexus.ui.theme.WarningForeground

@Composable
fun CheckInScreen(innerPadding: PaddingValues) {
    val repository = remember { AppGraph.repository }

    var accounts by remember { mutableStateOf(emptyList<WuwaAccount>()) }
    var cards by remember { mutableStateOf(emptyList<DashboardCardModel>()) }
    var results by remember { mutableStateOf(emptyList<CheckInResultUi>()) }

    LaunchedEffect(Unit) {
        accounts = repository.getBoundAccounts().filterWuwaAccounts()
        cards = repository.getCachedDashboardCards()
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
                text = "签到中心",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                NexusPrimaryButton(
                    label = "一键签到",
                    icon = Icons.Outlined.Bolt,
                    onClick = {
                        results = buildCheckInResults(accounts, cards)
                    },
                )
            }

            if (results.isEmpty()) {
                NexusEmptyStateCard(
                    title = "暂无签到记录",
                    icon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    results.forEach { result ->
                        CheckInResultCard(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInResultCard(result: CheckInResultUi) {
    val containerColor = when (result.tone) {
        CheckInTone.Success -> MaterialTheme.colorScheme.surface
        CheckInTone.Warning -> MaterialTheme.colorScheme.surface
        CheckInTone.Error -> ErrorBackground
    }
    val contentColor = when (result.tone) {
        CheckInTone.Success -> MaterialTheme.colorScheme.tertiary
        CheckInTone.Warning -> WarningForeground
        CheckInTone.Error -> ErrorForeground
    }

    NexusPanel(
        modifier = Modifier.fillMaxWidth(),
        containerColor = containerColor,
        borderColor = if (result.tone == CheckInTone.Error) ErrorForeground.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = result.icon,
                contentDescription = null,
                tint = contentColor,
            )
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = result.status,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
            )
        }
        Text(
            text = result.message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
        Text(
            text = result.caption,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
        )
        if (result.actionLabel != null) {
            NexusSecondaryButton(
                label = result.actionLabel,
                icon = Icons.Outlined.Key,
                onClick = {},
            )
        }
    }
}

private fun buildCheckInResults(
    accounts: List<WuwaAccount>,
    cards: List<DashboardCardModel>,
): List<CheckInResultUi> {
    if (accounts.isEmpty()) return emptyList()

    val cardByIdentity = cards.associateBy { "${it.title}:${it.subtitle}" }

    return accounts.take(3).mapIndexed { index, account ->
        val card = cardByIdentity["${account.roleName}:${account.serverName}"]
        when {
            card == null -> CheckInResultUi(
                title = "${account.nickname ?: account.roleName} - ${account.serverName}",
                status = "Token失效",
                message = "请重新绑定账号以继续签到",
                caption = "本地暂无可用快照",
                tone = CheckInTone.Error,
                icon = Icons.Outlined.ErrorOutline,
                actionLabel = "重新绑定",
            )

            card.signInStatus.contains("已") -> CheckInResultUi(
                title = "${account.nickname ?: account.roleName} - ${account.serverName}",
                status = if (index == 0) "已签到" else "今日已签到",
                message = if (index == 0) "同步快照：${card.energyText}" else "无新奖励",
                caption = "最后同步：${card.updatedAtText}",
                tone = if (index == 0) CheckInTone.Success else CheckInTone.Warning,
                icon = if (index == 0) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
            )

            else -> CheckInResultUi(
                title = "${account.nickname ?: account.roleName} - ${account.serverName}",
                status = "待执行",
                message = "已生成签到任务，等待后端接口接入后执行。",
                caption = "最近快照：${card.updatedAtText}",
                tone = CheckInTone.Warning,
                icon = Icons.Outlined.WarningAmber,
            )
        }
    }
}

private data class CheckInResultUi(
    val title: String,
    val status: String,
    val message: String,
    val caption: String,
    val tone: CheckInTone,
    val icon: ImageVector,
    val actionLabel: String? = null,
)

private fun List<WuwaAccount>.filterWuwaAccounts(): List<WuwaAccount> {
    return filter { it.gameId == GameType.WUWA.gameId }
}

private enum class CheckInTone {
    Success,
    Warning,
    Error,
}
