package com.nexus.game.wuwa.api

import com.nexus.game.wuwa.model.WuwaRoleEnvelopeDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface WuwaRoleApi {
    @FormUrlEncoded
    @POST("user/role/findUserDefaultRole")
    suspend fun findUserDefaultRole(
        @HeaderMap headers: Map<String, String>,
        @Field("queryUserId") queryUserId: String,
    ): WuwaRoleEnvelopeDto
}
