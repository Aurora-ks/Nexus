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
    val energyData: WuwaMetricDto,
    val livenessData: WuwaMetricDto,
    val battlePassData: List<WuwaMetricDto>,
    val storeEnergyData: WuwaMetricDto,
    val weeklyData: WuwaMetricDto,
    val weeklyRougeData: WuwaMetricDto,
    val towerData: WuwaMetricDto,
    val slashTowerData: WuwaMetricDto,
    val newTowerData: WuwaMetricDto = WuwaMetricDto.EMPTY,
    val weeklyFrameData: WuwaMetricDto = WuwaMetricDto.EMPTY,
)

@Serializable
data class WuwaMetricDto(
    val name: String,
    val img: String?,
    val key: String?,
    val value: String?,
    val status: Int,
    val cur: Int,
    val total: Int,
    val refreshTimeStamp: Long,
    val timePreDesc: String?,
    val expireTimeStamp: Long,
) {
    companion object {
        val EMPTY = WuwaMetricDto(
            name = "",
            img = null,
            key = null,
            value = null,
            status = 0,
            cur = 0,
            total = 0,
            refreshTimeStamp = 0,
            timePreDesc = null,
            expireTimeStamp = 0,
        )
    }
}
