package com.nexus.game.wuwa.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WuwaRoleEnvelopeDto(
    val code: Int,
    val msg: String,
    val success: Boolean,
    val traceId: String? = null,
    val data: WuwaRoleDataDto? = null,
)

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
    val userId: String,
    val isDefault: Boolean = false,
)
