package com.ehan.app3.network

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ChatApiProvider {

    private const val BASE_URL =
        "http://10.1.0.55:3000/"

    private val retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().build()
                )
            )
            .build()

    val api: ChatApi =
        retrofit.create(ChatApi::class.java)
}