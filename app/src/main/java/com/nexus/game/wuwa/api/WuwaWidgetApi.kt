package com.nexus.game.wuwa.api

import com.nexus.game.wuwa.model.WuwaWidgetEnvelopeDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface WuwaWidgetApi {
    @FormUrlEncoded
    @POST("aki/widget/getData")
    suspend fun getWidgetData(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: Int,
        @Field("roleId") roleId: String,
        @Field("serverId") serverId: String,
    ): WuwaWidgetEnvelopeDto
}
