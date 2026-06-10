package com.nexus.game.wuwa.api

import com.nexus.game.wuwa.model.WuwaRoleEnvelopeDto
import com.nexus.game.wuwa.model.WuwaRoleListEnvelopeDto
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface WuwaRoleApi {
    @FormUrlEncoded
    @POST("user/role/findUserDefaultRole")
    suspend fun findUserDefaultRole(
        @HeaderMap headers: Map<String, String>,
        @Field("queryUserId") queryUserId: String,
    ): WuwaRoleEnvelopeDto

    @FormUrlEncoded
    @POST("user/role/findRoleList")
    suspend fun findRoleList(
        @HeaderMap headers: Map<String, String>,
        @FieldMap fields: Map<String, String>,
    ): WuwaRoleListEnvelopeDto
}
