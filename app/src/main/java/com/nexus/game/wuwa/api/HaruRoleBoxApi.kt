package com.nexus.game.wuwa.api

import com.nexus.game.wuwa.model.HaruAccountDataEnvelopeDto
import com.nexus.game.wuwa.model.HaruDailyDataEnvelopeDto
import com.nexus.game.wuwa.model.HaruRefreshDataEnvelopeDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface HaruRoleBoxApi {
    @FormUrlEncoded
    @POST("haru/roleBox/accountData")
    suspend fun getAccountData(
        @HeaderMap headers: Map<String, String>,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
    ): HaruAccountDataEnvelopeDto

    @FormUrlEncoded
    @POST("haru/roleBox/dailyData")
    suspend fun getDailyData(
        @HeaderMap headers: Map<String, String>,
        @Field("type") type: Int = 2,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
    ): HaruDailyDataEnvelopeDto

    @FormUrlEncoded
    @POST("haru/roleBox/refreshData")
    suspend fun refreshData(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: String = "2",
        @Field("roleId") roleId: String,
        @Field("serverId") serverId: String,
    ): HaruRefreshDataEnvelopeDto
}
