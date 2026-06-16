package com.nexus.game.kuro.api

import com.nexus.game.kuro.model.KuroCheckInEnvelopeDto
import com.nexus.game.kuro.model.KuroCheckInInitEnvelopeDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface KuroCheckInApi {
    @FormUrlEncoded
    @POST("encourage/signIn/initSignInV2")
    suspend fun getCheckInInit(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: Int,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
        @Field("userId") userId: String,
    ): KuroCheckInInitEnvelopeDto

    @FormUrlEncoded
    @POST("encourage/signIn/v2")
    suspend fun checkIn(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: String,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
        @Field("userId") userId: String,
        @Field("reqMonth") reqMonth: String,
    ): KuroCheckInEnvelopeDto
}
