package com.nexus.feature.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nexus.app.AppGraph
import com.nexus.core.model.GameType
import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.DashboardDetailRowModel
import com.nexus.game.wuwa.model.DashboardMetricAccent
import com.nexus.game.wuwa.model.DashboardMetricModel
import com.nexus.game.wuwa.model.DashboardProgressModel
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusStatusChip
import com.nexus.ui.components.NexusStatusTone
import com.nexus.ui.theme.AccentPrimary
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.BorderSubtle
import com.nexus.ui.theme.SurfaceInput
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

    suspend fun reloadLocalState(refreshProfiles: Boolean = false) {
        accounts = if (refreshProfiles) {
            repository.refreshAccountProfiles()
        } else {
            repository.getBoundAccounts()
        }
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
        reloadLocalState(refreshProfiles = true)
        updateStatusMessage()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    reloadLocalState(refreshProfiles = true)
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
    val cardByIdentity = remember(cards) { cards.associateBy { "${it.title}:${it.subtitle}" } }
    var expandedAccountIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }

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
                    text = "今日状态",
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
                        text = "${overview.successCount} 已更新",
                        tone = NexusStatusTone.Success,
                    )
                    NexusStatusChip(
                        text = "${overview.pendingCount} 待同步",
                        tone = NexusStatusTone.Warning,
                    )
                    NexusStatusChip(
                        text = "${overview.alertCount} 异常",
                        tone = NexusStatusTone.Error,
                    )
                }
            }

            if (accounts.isEmpty()) {
                NexusEmptyStateCard(
                    title = "暂无账号概览",
                    description = "完成账号绑定后，这里会展示体力、签到与周常摘要。",
                    icon = Icons.Outlined.Refresh,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    accounts.forEach { account ->
                        val card = cardByIdentity["${account.roleName}:${account.serverName}"]
                        val expanded = expandedAccountIds.contains(account.id)
                        WuwaCollapsedCard(
                            account = account,
                            card = card,
                            expanded = expanded,
                            onToggleExpanded = {
                                expandedAccountIds = if (expanded) {
                                    expandedAccountIds - account.id
                                } else {
                                    expandedAccountIds + account.id
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
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

@Composable
private fun WuwaCollapsedCard(
    account: WuwaAccount,
    card: DashboardCardModel?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = card?.resourceMetrics.orEmpty().let { metrics ->
        if (metrics.isNotEmpty()) metrics else fallbackMetrics(account, card)
    }
    val detailRows = card?.detailRows.orEmpty().ifEmpty {
        card?.weeklyFocus.orEmpty().take(3).mapNotNull { item -> item.toDetailRowOrNull() }
    }
    val progress = card?.progress

    Surface(
        modifier = modifier,
        color = BackgroundWarm,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.animateContentSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AccountAvatar(
                    headPhotoUrl = account.headPhotoUrl,
                    modifier = Modifier.size(36.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = account.nickname ?: account.roleName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${account.gameDisplayName()} · ${card?.uidText ?: "UID ${account.roleId}"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )
                }
                CardToggleChip(
                    expanded = expanded,
                    onClick = onToggleExpanded,
                )
            }

            HorizontalDivider(color = BorderSubtle)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                metrics.take(3).forEachIndexed { index, metric ->
                    MetricColumn(
                        metric = metric,
                        modifier = Modifier.weight(1f),
                    )
                    if (index < 2) {
                        VerticalDivider()
                    }
                }
            }

            if (expanded && detailRows.isNotEmpty()) {
                HorizontalDivider(color = BorderSubtle)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    detailRows.forEachIndexed { index, row ->
                        DetailRow(
                            row = row,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (index != detailRows.lastIndex) {
                            HorizontalDivider(color = BorderSubtle)
                        }
                    }
                }
            }

            if (expanded && progress != null) {
                HorizontalDivider(color = BorderSubtle)
                ProgressSection(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "更新于 ${card?.updatedAtText ?: "待同步"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                )
            }
        }
    }
}

@Composable
private fun AccountAvatar(
    headPhotoUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (headPhotoUrl.isNullOrBlank()) {
        Surface(
            modifier = modifier,
            shape = androidx.compose.foundation.shape.CircleShape,
            color = SurfaceInput,
        ) {
            Spacer(modifier = Modifier.fillMaxSize())
        }
        return
    }
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(headPhotoUrl)
            .addHeader("User-Agent", "Kuro/2.11.0 KuroGameBox/2.11.0")
            .addHeader("Referer", "https://web-static.kurobbs.com/")
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier.clip(androidx.compose.foundation.shape.CircleShape),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun CardToggleChip(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(SurfaceInput)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (expanded) "收起" else "展开",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextMuted,
        )
        Icon(
            imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun MetricColumn(
    metric: DashboardMetricModel,
    modifier: Modifier = Modifier,
) {
    val accentColor = when (metric.accent) {
        DashboardMetricAccent.Primary -> AccentPrimary
        DashboardMetricAccent.Positive -> Color(0xFF6B8E4E)
    }

    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetricImage(
                imageUrl = metric.imageUrl,
                contentDescription = metric.label,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
            )
        }
        Text(
            text = metric.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = metric.caption,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(1.dp)
            .background(BorderSubtle),
    )
}

@Composable
private fun DetailRow(
    row: DashboardDetailRowModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetricImage(
            imageUrl = row.imageUrl,
            contentDescription = row.label,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = row.value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AccentPrimary,
                maxLines = 1,
            )
            if (!row.caption.isNullOrBlank()) {
                Text(
                    text = row.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(
    progress: DashboardProgressModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetricImage(
                imageUrl = progress.imageUrl,
                contentDescription = progress.label,
                modifier = Modifier.size(24.dp),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = progress.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    maxLines = 1,
                )
                if (!progress.levelText.isNullOrBlank()) {
                    Text(
                        text = progress.levelText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        maxLines = 1,
                    )
                }
            }
            Text(
                text = progress.value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(MaterialTheme.shapes.small)
                .background(SurfaceInput),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.progress.coerceIn(0f, 1f))
                    .clip(MaterialTheme.shapes.small)
                    .background(AccentPrimary),
            )
        }
    }
}

private fun fallbackMetrics(account: WuwaAccount, card: DashboardCardModel?): List<DashboardMetricModel> {
    if (account.gameId == GameType.PGR.gameId) {
        return listOf(
            DashboardMetricModel(
                label = "血清",
                value = card?.energyText?.substringAfter(' ') ?: "--/--",
                caption = card?.signInStatus ?: "--",
            ),
            DashboardMetricModel(
                label = "每日活跃",
                value = card?.weeklyFocus?.getOrNull(1)?.substringAfterLast(' ') ?: "--/--",
                caption = card?.weeklyFocus?.getOrNull(1)?.substringBeforeLast(' ') ?: "--",
            ),
            DashboardMetricModel(
                label = "委托情况",
                value = card?.weeklyFocus?.getOrNull(2)?.substringAfterLast(' ') ?: "--",
                caption = card?.weeklyFocus?.getOrNull(2)?.substringBeforeLast(' ') ?: "--",
            ),
        )
    }

    val energyText = card?.energyText?.substringAfter(' ') ?: "--/--"
    return listOf(
        DashboardMetricModel(
            label = "结晶波片",
            value = energyText,
            caption = card?.signInStatus ?: "--",
        ),
        DashboardMetricModel(
            label = "周常进度",
            value = card?.weeklyFocus?.getOrNull(0)?.substringAfterLast(' ') ?: "--/--",
            caption = card?.weeklyFocus?.getOrNull(0)?.substringBeforeLast(' ') ?: "--",
        ),
        DashboardMetricModel(
            label = "活跃度",
            value = card?.weeklyFocus?.getOrNull(1)?.substringAfterLast(' ') ?: "--/--",
            caption = card?.weeklyFocus?.getOrNull(1)?.substringBeforeLast(' ') ?: "--",
        ),
    )
}

@Composable
private fun MetricImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 4.dp,
) {
    if (imageUrl.isNullOrBlank()) {
        Spacer(modifier = modifier)
        return
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)),
        contentScale = ContentScale.Crop,
    )
}

private fun String.toDetailRowOrNull(): DashboardDetailRowModel? {
    val splitIndex = lastIndexOf(' ')
    if (splitIndex <= 0 || splitIndex >= lastIndex) return null
    return DashboardDetailRowModel(
        label = substring(0, splitIndex),
        value = substring(splitIndex + 1),
    )
}

private fun WuwaAccount.gameDisplayName(): String {
    return when (gameId) {
        GameType.WUWA.gameId -> "鸣潮"
        GameType.PGR.gameId -> "战双帕弥什"
        else -> "未知游戏"
    }
}

private data class DashboardOverviewState(
    val summaryText: String,
    val successCount: Int,
    val pendingCount: Int,
    val alertCount: Int,
)

private fun buildOverviewState(
    accounts: List<WuwaAccount>,
    cards: List<DashboardCardModel>,
): DashboardOverviewState {
    val successCount = cards.size
    val pendingCount = (accounts.size - cards.size).coerceAtLeast(0)
    val alertCount = cards.count { it.signInStatus.contains("失效") || it.signInStatus.contains("失败") }

    val summaryText = when {
        accounts.isEmpty() -> "还没有接入任何账号，先去账号页完成 Token 绑定。"
        cards.isEmpty() -> "已接入 ${accounts.size} 个账号，当前还没有可用的同步摘要。"
        else -> "${accounts.size} 个账号已接入，其中 ${successCount.coerceAtMost(accounts.size)} 个已有最新摘要，${pendingCount} 个待同步。"
    }

    return DashboardOverviewState(
        summaryText = summaryText,
        successCount = successCount.coerceAtLeast(0),
        pendingCount = pendingCount.coerceAtLeast(0),
        alertCount = alertCount.coerceAtLeast(0),
    )
}
