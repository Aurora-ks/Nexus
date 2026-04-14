package com.nexus.game.wuwa.model

import kotlinx.serialization.Serializable

@Serializable
data class WuwaEnvelopeDto<T>(
    val code: Int,
    val msg: String,
    val success: Boolean,
    val data: T? = null,
)
