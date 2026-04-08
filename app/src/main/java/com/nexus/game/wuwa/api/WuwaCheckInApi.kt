package com.nexus.game.wuwa.api

import com.nexus.game.wuwa.model.WuwaCheckInEnvelopeDto
import com.nexus.game.wuwa.model.WuwaCheckInInitEnvelopeDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface WuwaCheckInApi {
    @FormUrlEncoded
    @POST("encourage/signIn/initSignInV2")
    suspend fun getCheckInInit(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: Int,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
        @Field("userId") userId: String,
    ): WuwaCheckInInitEnvelopeDto

    @FormUrlEncoded
    @POST("encourage/signIn/v2")
    suspend fun checkIn(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: Int,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
        @Field("userId") userId: String,
        @Field("reqMonth") reqMonth: String,
    ): WuwaCheckInEnvelopeDto
}
