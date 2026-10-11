package com.ehan.app3.bot.tiktok

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TikWmApi {
    @GET("/")
    suspend fun fetchTikTok(
        @Query("url") url: String,
        @Query("hd") hd: String = "1",
        @Header("x-tikwmapi-key") apiKey: String
    ): Response<TikWmResponse>
}
