package com.nexus.game.wuwa.api

import com.nexus.game.wuwa.model.WuwaRefreshDataEnvelopeDto
import com.nexus.game.wuwa.model.WuwaRequestTokenEnvelopeDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface WuwaAkiBoxApi {
    @FormUrlEncoded
    @POST("aki/roleBox/requestToken")
    suspend fun requestToken(
        @HeaderMap headers: Map<String, String>,
        @Field("roleId") roleId: String,
        @Field("serverId") serverId: String,
        @Field("userId") userId: String,
    ): WuwaRequestTokenEnvelopeDto

    @FormUrlEncoded
    @POST("aki/roleBox/akiBox/refreshData")
    suspend fun refreshData(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: Int,
        @Field("roleId") roleId: String,
        @Field("serverId") serverId: String,
    ): WuwaRefreshDataEnvelopeDto
}
