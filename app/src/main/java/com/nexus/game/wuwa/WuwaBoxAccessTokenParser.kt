package com.nexus.game.wuwa

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

object WuwaBoxAccessTokenParser {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun parse(rawData: String): String {
        val element = json.parseToJsonElement(rawData)
        return when (element) {
            is JsonPrimitive -> element.content
            is JsonObject -> {
                val payload = json.decodeFromJsonElement<WuwaBoxAccessTokenPayloadDto>(element)
                payload.accessToken ?: throw IllegalArgumentException("响应中缺少 b-at")
            }

            else -> throw IllegalArgumentException("无法解析 b-at 响应")
        }.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("响应中的 b-at 为空")
    }

    @Serializable
    private data class WuwaBoxAccessTokenPayloadDto(
        val accessToken: String? = null,
    )
}
