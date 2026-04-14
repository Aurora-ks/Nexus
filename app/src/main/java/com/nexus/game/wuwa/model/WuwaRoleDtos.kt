package com.nexus.game.wuwa.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias WuwaRoleEnvelopeDto = WuwaEnvelopeDto<WuwaRoleDataDto>

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
    val userId: String, // 库街区userID
)
