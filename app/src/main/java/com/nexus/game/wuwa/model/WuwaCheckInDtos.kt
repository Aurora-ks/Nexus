package com.nexus.game.wuwa.model

import kotlinx.serialization.Serializable

typealias WuwaCheckInInitEnvelopeDto = WuwaEnvelopeDto<WuwaCheckInInitDto>

@Serializable
data class WuwaCheckInInitDto(
    val isSigIn: Boolean,
    val sigInNum: Int,
    val nowServerTimes: String? = null,
)

typealias WuwaCheckInEnvelopeDto = WuwaEnvelopeDto<WuwaCheckInRewardDataDto>

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
