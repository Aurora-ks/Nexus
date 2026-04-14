package com.nexus.game.wuwa.model

import kotlinx.serialization.Serializable

typealias WuwaWidgetEnvelopeDto = WuwaEnvelopeDto<WuwaWidgetDataDto>

@Serializable
data class WuwaWidgetDataDto(
    val gameId: Int,
    val userId: Long,
    val serverTime: Long,
    val serverId: String,
    val serverName: String,
    val signInTxt: String,
    val hasSignIn: Boolean,
    val roleId: String,
    val roleName: String,
    val energyData: WuwaMetricDto? = null,
    val livenessData: WuwaMetricDto? = null,
    val weeklyData: WuwaMetricDto? = null,
    val weeklyRougeData: WuwaMetricDto? = null,
    val towerData: WuwaMetricDto? = null,
    val slashTowerData: WuwaMetricDto? = null,
)

@Serializable
data class WuwaMetricDto(
    val name: String? = null,
    val value: String? = null,
    val status: Int = 0,
    val cur: Int = 0,
    val total: Int = 0,
    val refreshTimeStamp: Long = 0,
    val expireTimeStamp: Long = 0,
)
