package com.nexus.game.kuro.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

typealias KuroRoleEnvelopeDto = KuroEnvelopeDto<KuroRoleDataDto>
typealias KuroRoleListEnvelopeDto = KuroEnvelopeDto<JsonElement>

@Serializable
data class KuroEnvelopeDto<T>(
    val code: Int,
    val msg: String,
    val success: Boolean,
    val data: T? = null,
)

@Serializable
data class KuroRoleDataDto(
    @SerialName("defaultRoleList")
    val defaultRoleList: List<KuroRoleDto> = emptyList(),
)

@Serializable
data class KuroRoleDto(
    val gameId: Int,
    val roleId: String,
    val roleName: String,
    val serverId: String,
    val serverName: String,
    val headPhotoUrl: String? = null,
    val userId: String, // 库街区userID
)
