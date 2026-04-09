package com.nexus.core.network

import com.nexus.app.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nexus.game.wuwa.api.WuwaRoleApi
import com.nexus.game.wuwa.api.WuwaWidgetApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object NetworkModule {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val okHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(DebugHttpLoggingInterceptor())
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConstants.BaseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val wuwaRoleApi: WuwaRoleApi = retrofit.create(WuwaRoleApi::class.java)
    val wuwaWidgetApi: WuwaWidgetApi = retrofit.create(WuwaWidgetApi::class.java)
}
