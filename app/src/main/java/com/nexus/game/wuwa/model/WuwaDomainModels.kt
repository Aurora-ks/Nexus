package com.nexus.game.wuwa.model

import com.nexus.core.model.GameType

data class WuwaAccount(
    val id: Long = 0,
    val gameId: Int = GameType.WUWA.gameId,
    val userId: String,
    val roleId: String,
    val roleName: String,
    val serverId: String,
    val serverName: String,
    val nickname: String? = null,
)

data class DashboardCardModel(
    val title: String,
    val subtitle: String,
    val uidText: String = "",
    val energyText: String,
    val signInStatus: String,
    val updatedAtText: String,
    val weeklyFocus: List<String>,
    val resourceMetrics: List<DashboardMetricModel> = emptyList(),
    val detailRows: List<DashboardDetailRowModel> = emptyList(),
    val progress: DashboardProgressModel? = null,
)

data class DashboardMetricModel(
    val label: String,
    val value: String,
    val caption: String,
    val imageUrl: String? = null,
    val accent: DashboardMetricAccent = DashboardMetricAccent.Primary,
)

enum class DashboardMetricAccent {
    Primary,
    Positive,
}

data class DashboardDetailRowModel(
    val label: String,
    val value: String,
    val caption: String? = null,
    val imageUrl: String? = null,
)

data class DashboardProgressModel(
    val label: String,
    val levelText: String? = null,
    val value: String,
    val progress: Float,
    val imageUrl: String? = null,
)
