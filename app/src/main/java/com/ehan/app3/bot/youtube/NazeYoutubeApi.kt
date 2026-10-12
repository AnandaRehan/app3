package com.ehan.app3.bot.youtube

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NazeYoutubeApi {

    @GET("api/v1/download/ytmp4")
    suspend fun fetchYtMp4(
        @Query("url") url: String,
        @Query("quality") quality: String = "720",
        @Query("apikey") apiKey: String
    ): Response<NazeYtMp4Response>

    @GET("api/v1/download/ytmp3-flvto")
    suspend fun fetchYtMp3(
        @Query("url") url: String,
        @Query("quality") quality: String = "720",
        @Query("apikey") apiKey: String
    ): Response<NazeYtMp3Response>
}
