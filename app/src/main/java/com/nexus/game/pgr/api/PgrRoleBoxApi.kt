package com.nexus.game.pgr.api

import com.nexus.game.pgr.model.PgrAccountDataEnvelopeDto
import com.nexus.game.pgr.model.PgrDailyDataEnvelopeDto
import com.nexus.game.pgr.model.PgrRefreshDataEnvelopeDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface PgrRoleBoxApi {
    @FormUrlEncoded
    @POST("haru/roleBox/accountData")
    suspend fun getAccountData(
        @HeaderMap headers: Map<String, String>,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
    ): PgrAccountDataEnvelopeDto

    @FormUrlEncoded
    @POST("haru/roleBox/dailyData")
    suspend fun getDailyData(
        @HeaderMap headers: Map<String, String>,
        @Field("type") type: Int = 2,
        @Field("serverId") serverId: String,
        @Field("roleId") roleId: String,
    ): PgrDailyDataEnvelopeDto

    @FormUrlEncoded
    @POST("haru/roleBox/refreshData")
    suspend fun refreshData(
        @HeaderMap headers: Map<String, String>,
        @Field("gameId") gameId: String = "2",
        @Field("roleId") roleId: String,
        @Field("serverId") serverId: String,
    ): PgrRefreshDataEnvelopeDto
}
