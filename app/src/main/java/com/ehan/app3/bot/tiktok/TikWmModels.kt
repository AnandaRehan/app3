package com.ehan.app3.bot.tiktok

import com.google.gson.annotations.SerializedName

data class TikWmResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("processed_time") val processedTime: Double? = null,
    @SerializedName("data") val data: TikWmData? = null
)

data class TikWmData(
    @SerializedName("aweme_id") val awemeId: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("content_desc") val contentDesc: List<String>? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("ai_dynamic_cover") val aiDynamicCover: String? = null,
    @SerializedName("origin_cover") val originCover: String? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("play") val play: String? = null,
    @SerializedName("wmplay") val wmplay: String? = null,
    @SerializedName("hdplay") val hdplay: String? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("wm_size") val wmSize: Long? = null,
    @SerializedName("hd_size") val hdSize: Long? = null,
    @SerializedName("music") val music: String? = null,
    @SerializedName("music_info") val musicInfo: TikWmMusicInfo? = null,
    @SerializedName("play_count") val playCount: Long? = null,
    @SerializedName("digg_count") val diggCount: Long? = null,
    @SerializedName("comment_count") val commentCount: Long? = null,
    @SerializedName("share_count") val shareCount: Long? = null,
    @SerializedName("download_count") val downloadCount: Long? = null,
    @SerializedName("collect_count") val collectCount: Long? = null,
    @SerializedName("create_time") val createTime: Long? = null,
    @SerializedName("is_ad") val isAd: Boolean? = null,
    @SerializedName("author") val author: TikWmAuthor? = null,
    @SerializedName("images") val images: List<String>? = null,
    @SerializedName("live_images") val liveImages: List<String?>? = null
) {
    val isPhotoSlide: Boolean
        get() = !images.isNullOrEmpty()
}

data class TikWmMusicInfo(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("play") val play: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("original") val original: Boolean? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("album") val album: String? = null,
    @SerializedName("create_time") val createTime: Long? = null
)

data class TikWmAuthor(
    @SerializedName("id") val id: String? = null,
    @SerializedName("unique_id") val uniqueId: String? = null,
    @SerializedName("nickname") val nickname: String? = null,
    @SerializedName("avatar") val avatar: String? = null
)
