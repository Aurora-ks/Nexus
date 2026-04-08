package com.nexus.game.wuwa.model

import kotlinx.serialization.Serializable

@Serializable
data class WuwaCheckInInitEnvelopeDto(
    val code: Int,
    val msg: String,
    val success: Boolean,
    val traceId: String? = null,
    val data: WuwaCheckInInitDto? = null,
)

@Serializable
data class WuwaCheckInInitDto(
    val isSigIn: Boolean,
    val sigInNum: Int,
    val nowServerTimes: String? = null,
)

@Serializable
data class WuwaCheckInEnvelopeDto(
    val code: Int,
    val msg: String,
    val success: Boolean,
    val traceId: String? = null,
    val data: WuwaCheckInRewardDataDto? = null,
)

@Serializable
data class WuwaCheckInRewardDataDto(
    val todayList: List<WuwaRewardDto> = emptyList(),
    val tomorrowList: List<WuwaRewardDto> = emptyList(),
)

@Serializable
data class WuwaRewardDto(
    val goodsId: Int,
    val goodsNum: Int,
    val goodsUrl: String,
    val type: Int,
)
