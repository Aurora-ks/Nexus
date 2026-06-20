package com.nexus.feature.checkin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.core.model.GameType
import com.nexus.ui.components.NexusAvatar
import com.nexus.ui.components.NexusEmptyStateCard
import com.nexus.ui.components.NexusPage
import com.nexus.ui.components.NexusPanel
import com.nexus.ui.components.NexusPrimaryButton
import com.nexus.ui.components.NexusSecondaryButton
import com.nexus.ui.components.NexusStatusChip
import com.nexus.ui.components.NexusStatusTone
import com.nexus.ui.theme.AccentPrimary
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.BorderSubtle
import com.nexus.ui.theme.ErrorForeground
import com.nexus.ui.theme.SurfaceCard
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    innerPadding: PaddingValues,
    viewModel: CheckInViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = viewModel::refresh,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWarm)
            .statusBarsPadding()
            .padding(innerPadding),
    ) {
        NexusPage(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "签到中心",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
                NexusPrimaryButton(
                    label = if (uiState.isBatchSigningIn) "签到中" else "一键签到",
                    icon = Icons.Outlined.CheckCircle,
                    enabled = uiState.canCheckInAll,
                    onClick = viewModel::checkInAll,
                )
            }

            when {
                uiState.isLoading && uiState.accounts.isEmpty() -> LoadingPanel()
                uiState.accounts.isEmpty() -> NexusEmptyStateCard(
                    title = "暂无签到记录",
                    description = "绑定鸣潮账号后会展示每个账号的签到状态",
                    icon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.fillMaxWidth(),
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.accounts.forEach { account ->
                        CheckInAccountCard(
                            account = account,
                            onCheckIn = viewModel::checkIn,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingPanel() {
    NexusPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AccentPrimary,
                strokeWidth = 2.dp,
            )
            Text(
                text = "正在获取签到状态",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun CheckInAccountCard(
    account: CheckInAccountUiState,
    onCheckIn: (Long) -> Unit,
) {
    NexusPanel(
        modifier = Modifier.fillMaxWidth(),
        containerColor = SurfaceCard,
        borderColor = BorderSubtle,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckInAvatar(account = account)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = account.roleName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    NexusStatusChip(
                        text = account.statusText,
                        tone = account.statusTone,
                    )
                }
                Text(
                    text = "${account.gameDisplayName()} · ${account.uidText}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                )
                val detailText = account.errorMessage
                    ?: "本期已签到 ${account.signedDays} 天"
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (account.errorMessage == null) TextSecondary else ErrorForeground,
                )
            }
            CheckInStateButton(
                account = account,
                onCheckIn = onCheckIn,
            )
        }
    }
}

@Composable
private fun CheckInAvatar(account: CheckInAccountUiState) {
    if (account.headPhotoUrl.isNullOrBlank()) {
        NexusAvatar(label = account.roleName)
        return
    }
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(account.headPhotoUrl)
            .addHeader("User-Agent", "Kuro/2.11.0 KuroGameBox/2.11.0")
            .addHeader("Referer", "https://web-static.kurobbs.com/")
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun CheckInStateButton(
    account: CheckInAccountUiState,
    onCheckIn: (Long) -> Unit,
) {
    if (account.errorMessage != null) {
        Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = ErrorForeground,
            modifier = Modifier.size(22.dp),
        )
        return
    }

    val label = if (account.isSignedIn) "已签到" else "签到"
    NexusSecondaryButton(
        label = label,
        icon = Icons.Outlined.CheckCircle,
        enabled = !account.isSignedIn && !account.isSigningIn,
        onClick = { onCheckIn(account.accountId) },
        containerColor = AccentPrimary,
        contentColor = Color.White,
        disabledContainerColor = Color(0xFFD6CEC6),
        disabledContentColor = Color(0xFF8A827B),
    )
}

private val CheckInAccountUiState.statusTone: NexusStatusTone
    get() = when {
        errorMessage != null -> NexusStatusTone.Error
        isSigningIn -> NexusStatusTone.Warning
        isSignedIn -> NexusStatusTone.Success
        else -> NexusStatusTone.Warning
    }

private fun CheckInAccountUiState.gameDisplayName(): String {
    return when (gameId) {
        GameType.WUWA.gameId -> "鸣潮"
        GameType.PGR.gameId -> "战双帕弥什"
        else -> "未知游戏"
    }
}
