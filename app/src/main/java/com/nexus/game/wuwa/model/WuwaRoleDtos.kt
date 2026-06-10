package com.nexus.game.wuwa.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

typealias WuwaRoleEnvelopeDto = WuwaEnvelopeDto<WuwaRoleDataDto>
typealias WuwaRoleListEnvelopeDto = WuwaEnvelopeDto<JsonElement>

@Serializable
data class WuwaRoleDataDto(
    @SerialName("defaultRoleList")
    val defaultRoleList: List<WuwaRoleDto> = emptyList(),
)

@Serializable
data class WuwaRoleDto(
    val gameId: Int,
    val roleId: String,
    val roleName: String,
    val serverId: String,
    val serverName: String,
    val headPhotoUrl: String? = null,
    val userId: String, // 库街区userID
)
