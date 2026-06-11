package com.nexus.game.wuwa.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

typealias HaruAccountDataEnvelopeDto = WuwaEnvelopeDto<HaruAccountDataDto>
typealias HaruDailyDataEnvelopeDto = WuwaEnvelopeDto<HaruDailyDataDto>
typealias HaruRefreshDataEnvelopeDto = WuwaEnvelopeDto<JsonElement>

@Serializable
data class HaruAccountDataDto(
    val roleId: String,
    val level: Int,
    val roleName: String,
    val serverName: String,
    val headIconUrl: String,
    val rank: String? = null,
)

@Serializable
data class HaruDailyDataDto(
    val serverTime: Long,
    val actionData: HaruDailyDataItemDto,
    val dormData: HaruDailyDataItemDto,
    val activeData: HaruDailyDataItemDto,
    val bossData: List<HaruDailyDataItemDto> = emptyList(),
    val temporaryClose: Boolean = false,
)

@Serializable
data class HaruDailyDataItemDto(
    val name: String? = null,
    val key: String? = null,
    val refreshTimeStamp: Long? = null,
    val expireTimeStamp: Long? = null,
    val value: String? = null,
    val status: Int = 0,
    val cur: Int = 0,
    val total: Int = 0,
)
