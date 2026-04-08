package com.nexus.game.wuwa

import com.nexus.core.model.AppError
import com.nexus.core.model.OperationResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

object TokenParser {
    fun parseUserId(token: String): OperationResult<String> {
        val parts = token.split(".")
        if (parts.size < 2) {
            return OperationResult.Failure(AppError.ParseError("Token payload is missing"))
        }

        val payload = runCatching {
            val normalized = parts[1]
                .replace('-', '+')
                .replace('_', '/')
                .let { segment ->
                    segment + "=".repeat((4 - segment.length % 4) % 4)
                }
            String(Base64.getDecoder().decode(normalized))
        }.getOrElse {
            return OperationResult.Failure(AppError.ParseError("Token payload is not valid base64"))
        }

        val userId = runCatching {
            Json.parseToJsonElement(payload)
                .jsonObject["userId"]
                ?.jsonPrimitive
                ?.content
        }.getOrNull()

        return if (userId.isNullOrBlank()) {
            OperationResult.Failure(AppError.ParseError("Token payload does not contain userId"))
        } else {
            OperationResult.Success(userId)
        }
    }
}
