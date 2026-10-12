package com.ehan.app3.bot.youtube

import com.google.gson.annotations.SerializedName

data class NazeYtMp4Response(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("result") val result: NazeYtMp4Result? = null
)

data class NazeYtMp4Result(
    @SerializedName("title") val title: String? = null,
    @SerializedName("ext") val ext: String? = null,
    @SerializedName("filesize") val filesize: Long? = null,
    @SerializedName("download_url") val downloadUrl: String? = null,
    @SerializedName("is60fps") val is60fps: Boolean? = null
)

data class NazeYtMp3Response(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("result") val result: NazeYtMp3Result? = null
)

data class NazeYtMp3Result(
    @SerializedName("link") val link: String? = null,
    @SerializedName("progress") val progress: Int? = null,
    @SerializedName("pc") val pc: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("duration") val duration: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("filesize") val filesize: Long? = null
)
