package com.nexus.game.wuwa.model

data class WuwaAccount(
    val id: Long = 0,
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
    val energyText: String,
    val signInStatus: String,
    val updatedAtText: String,
    val weeklyFocus: List<String>,
)
