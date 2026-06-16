package com.nexus.game.kuro.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

typealias KuroCheckInInitEnvelopeDto = KuroEnvelopeDto<KuroCheckInInitDto>

@Serializable
data class KuroCheckInInitDto(
    val isSigIn: Boolean, // 今日是否签到
    val sigInNum: Int, // 已签到次数
    val nowServerTimes: String? = null,
    val eventEndTimes: String? = null,
    val eventStartTimes: String? = null,
)

typealias KuroCheckInEnvelopeDto = KuroEnvelopeDto<JsonElement>

data class KuroCheckInInfo(
    val accountId: Long,
    val roleName: String,
    val serverName: String,
    val isSignedIn: Boolean,
    val signedDays: Int,
    val serverTime: String?,
    val eventStartTime: String?,
    val eventEndTime: String?,
    val missedDays: Int,
    val replenishCount: Int,
    val rewards: List<KuroCheckInRewardModel>,
)

data class KuroCheckInRewardModel(
    val goodsId: Int,
    val goodsName: String,
    val goodsNum: Int,
    val goodsUrl: String,
    val serialNum: Int,
    val isGain: Boolean,
)
