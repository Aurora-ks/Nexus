package com.nexus.game.pgr.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

typealias PgrAccountDataEnvelopeDto = PgrEnvelopeDto<PgrAccountDataDto>
typealias PgrDailyDataEnvelopeDto = PgrEnvelopeDto<PgrDailyDataDto>
typealias PgrRefreshDataEnvelopeDto = PgrEnvelopeDto<JsonElement>

@Serializable
data class PgrEnvelopeDto<T>(
    val code: Int,
    val msg: String,
    val success: Boolean,
    val data: T? = null,
)

@Serializable
data class PgrAccountDataDto(
    val roleId: String,
    val level: Int,
    val roleName: String,
    val serverName: String,
    val headIconUrl: String,
    val rank: String? = null,
)

@Serializable
data class PgrDailyDataDto(
    val serverTime: Long,
    val actionData: PgrDailyDataItemDto,
    val dormData: PgrDailyDataItemDto,
    val activeData: PgrDailyDataItemDto,
    val bossData: List<PgrDailyDataItemDto> = emptyList(),
    val temporaryClose: Boolean = false,
)

@Serializable
data class PgrDailyDataItemDto(
    val name: String? = null,
    val key: String? = null,
    val refreshTimeStamp: Long? = null,
    val expireTimeStamp: Long? = null,
    val value: String? = null,
    val status: Int = 0,
    val cur: Int = 0,
    val total: Int = 0,
)
