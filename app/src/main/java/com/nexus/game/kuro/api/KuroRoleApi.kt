package com.nexus.game.kuro.api

import com.nexus.game.kuro.model.KuroRoleEnvelopeDto
import com.nexus.game.kuro.model.KuroRoleListEnvelopeDto
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface KuroRoleApi {
    @FormUrlEncoded
    @POST("user/role/findUserDefaultRole")
    suspend fun findUserDefaultRole(
        @HeaderMap headers: Map<String, String>,
        @Field("queryUserId") queryUserId: String,
    ): KuroRoleEnvelopeDto

    @FormUrlEncoded
    @POST("user/role/findRoleList")
    suspend fun findRoleList(
        @HeaderMap headers: Map<String, String>,
        @FieldMap fields: Map<String, String>,
    ): KuroRoleListEnvelopeDto
}
